# Sicherheit im Deployment-Pipeline

Dieses Dokument ordnet jede Sicherheitsebene aus der IPA-Präsentation einem konkreten Job in unserer CI/CD-Pipeline zu. Jede Spalte der Tabellen ist während der Demo nachprüfbar: **Ort** zeigt, wo das Ergebnis in GitHub sichtbar ist.

## Übersicht — alle Layer auf einen Blick

| Präsentations-Layer          | Tool                                           | Job (Workflow)                     | Ort des Ergebnisses                                   |
| ---------------------------- | ---------------------------------------------- | ---------------------------------- | ----------------------------------------------------- |
| Shift-Left · SAST            | GitHub CodeQL                                  | `security-sast` (`ci.yml`)         | Security → Code scanning alerts                       |
| Shift-Left · Secret Scanning | gitleaks                                       | `security-secrets` (`ci.yml`)      | Actions-Log; hard-fail bei Fund                       |
| Shift-Left · SCA             | OSV-Scanner ¹                                  | `security-deps` (`ci.yml`)         | Security → Code scanning alerts                       |
| IaC-Scanning                 | tfsec                                          | `security-iac` (`ci.yml`)          | Security → Code scanning alerts                       |
| Dockerfile Lint              | Hadolint                                       | `security-dockerfiles` (`ci.yml`)  | Security → Code scanning alerts                       |
| Build · Container Scan       | Trivy                                          | `security-containers` (`ci.yml`)   | Security → Code scanning alerts                       |
| Build · SBOM                 | Syft (Anchore)                                 | `security-supply-chain` (`ci.yml`) | Actions → Run → Artifacts (`sbom-*.spdx.json`)        |
| Build · Signierte Artefakte  | Cosign (Sigstore, keyless)                     | `security-supply-chain` (`ci.yml`) | Artifact Registry Tag `*.sig`; Rekor Transparency Log |
| Build · SLSA Provenance      | `actions/attest-build-provenance`              | `security-supply-chain` (`ci.yml`) | Repo → Attestations                                   |
| Deploy · Secrets Management  | GCP Secret Manager                             | `infra/modules/secrets` (Tofu)     | GCP Console → Secret Manager                          |
| Deploy · Least Privilege IAM | Per-Secret IAM binding, dediziertes Runtime-SA | `infra/modules/iam` (Tofu)         | GCP Console → IAM                                     |
| Runtime · DAST               | OWASP ZAP Baseline                             | `zap-baseline` (`dast.yml`)        | Actions → ZAP run → Artifact                          |
| Runtime · Monitoring         | Cloud Run Logs/Metrics (nativ)                 | GCP automatisch                    | GCP Console → Cloud Run → Logs/Metrics                |

¹ OSV-Scanner nutzt dieselbe OSV/NVD-Datenbank wie OWASP Dependency-Check. Ein Tool deckt `pnpm-lock.yaml` und `pom.xml` ab.

## Datenbank-Migrationen

Frage aus dem Präsentations-Entwurf: _"Wo sind die Migrationen für die Datenbank?"_

- **Ort:** `backend/src/main/resources/db/migration/`
- **Dateien:** aktuell `V1__initial_schema.sql` und `V2__add_favorites.sql`
- **Konvention:** `V{nummer}__snake_case_beschreibung.sql`
- **Konfiguration:** `backend/src/main/resources/application.yml` unter `spring.flyway` (enabled, baseline-on-migrate = true).

### Ausführung in der Pipeline (zweistufig)

1. **`migrate`-Job (Pre-Deploy-Gate)** — läuft nach `build-push`, bevor `deploy` startet:
   - Cloud SQL Auth Proxy öffnet einen lokalen TCP-Tunnel zur prod-DB (keine Public-IP-Exposition).
   - DB-Credentials werden aus GCP Secret Manager (`loete-database-username`, `loete-database-password`) geholt — nie im Repo, nie im Workflow-Log.
   - Flyway CLI führt `flyway migrate` gegen `127.0.0.1:5432` aus.
   - Schlägt eine Migration fehl, läuft `deploy` **nicht** an — die laufende Cloud-Run-Revision bleibt unberührt.
2. **Flyway beim Boot (Safety-Net)** — Spring Boot führt beim Start `flyway migrate` erneut aus; in Normalbetrieb ist das ein No-Op. Schützt bei Rollback oder Notfall-Revisionen.

Damit ist die Migration **automatisiert, nachvollziehbar (CI-Log zeigt jede angewendete Version) und atomar pro Deploy** — ein kaputtes Migration-Skript kann keine tote Cloud-Run-Revision erzeugen, weil der Deploy vorher gestoppt wird.

## Verifikations-Kommandos (live auf der Bühne)

### 1. Cosign-Signatur prüfen

```sh
IMAGE="europe-west1-docker.pkg.dev/<PROJECT_ID>/loete/backend"
DIGEST=$(gcloud artifacts docker images describe "$IMAGE:latest" --format='value(image_summary.digest)')

cosign verify "$IMAGE@$DIGEST" \
  --certificate-identity-regexp "https://github.com/<OWNER>/loete/\.github/workflows/ci\.yml@refs/heads/main" \
  --certificate-oidc-issuer https://token.actions.githubusercontent.com
```

Erwartete Ausgabe: Transparency-Log-Eintrag mit OIDC-Identity, die auf unseren Workflow zeigt. Das beweist: dieses Image wurde von genau diesem Workflow auf `main` gebaut, nicht lokal gepusht.

### 2. SLSA-Provenance prüfen

```sh
gh attestation verify oci://$IMAGE@$DIGEST --repo <OWNER>/loete
```

### 3. SBOM einsehen

GitHub Actions UI → letzter `CI/CD`-Run → Artifacts → `sbom-backend-<sha>.spdx.json` herunterladen und öffnen. Enthält jede direkte und transitive Dependency.

### 4. Findings in der Security-Tab

Repo → **Security** → **Code scanning alerts**. Filter `Tool`:

- `CodeQL` → SAST Findings
- `OSV-Scanner` → verwundbare Dependencies
- `tfsec` → riskante Tofu-Konfigurationen
- `Trivy` → Container-Image CVEs
- `Hadolint` → Dockerfile Anti-Patterns

## Was ist bewusst _nicht_ in der Pipeline

| Thema                                            | Warum nicht                                                                | Was wir stattdessen zeigen                                                                                               |
| ------------------------------------------------ | -------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Falco (Runtime Protection)                       | Falco ist ein Kubernetes-eBPF-Tool; wir laufen auf Cloud Run (serverless). | Cloud Run Logging + Cloud Monitoring decken Runtime-Anomalien ab.                                                        |
| OWASP Dependency-Check zusätzlich zu OSV-Scanner | Doppelte Abdeckung, 3× CI-Zeit.                                            | OSV-Scanner nutzt dieselbe Datenbank-Quelle.                                                                             |
| Hard-Fail-Gates bei neuen Scannern               | Demo soll grün laufen; Findings sind sichtbar, blockieren aber nicht.      | `security-secrets` (gitleaks) ist die einzige Ausnahme — hier blockieren wir, weil ein Secret-Commit immer kritisch ist. |

## Pipeline-Ausführung (Deployment-Strategie)

Die Pipeline implementiert ein **Rolling Update** über Cloud Run:

- `build-push` → `deploy` → `infra` laufen nur auf `main`-Push.
- Cloud Run ersetzt Revisionen nahtlos ohne Downtime (traffic-splitting auf Revisionsebene).
- Blue/Green oder Canary wären per Cloud Run Traffic-Splitting erreichbar, sind aber für diesen Umfang überdimensioniert — im Slide-Deck als "nächste Ausbaustufe" referenziert.
