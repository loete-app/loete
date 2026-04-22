# Löte Infrastructure

OpenTofu configuration for Löte on Google Cloud Platform. Region `europe-west6` (Zurich).

## Architecture

- **Cloud Run** — two services: `loete-backend` (Spring Boot, port 8080) and `loete-frontend` (Angular SSR, port 4000)
- **Cloud SQL** — PostgreSQL 16, connected to the backend via the Cloud SQL Unix socket (`/cloudsql/...`)
- **Artifact Registry** — Docker repository `loete` with `backend` and `frontend` images
- **Secret Manager** — `database-url`, `database-username`, `database-password`, `cors-origins`, `jwt-secret`, `ticketmaster-api-key`
- **IAM** — runtime SA `loete-run` (least-privilege); deployer SA `loete-github` used by GitHub Actions via Workload Identity Federation

## One-time bootstrap (manual)

These steps are required once before the first `tofu apply`:

1. **Create the GCP project** and enable billing.
2. **Create the Tofu state bucket:**
   ```sh
   gcloud storage buckets create gs://loete-tofu-state \
     --project=PROJECT_ID --location=europe-west6 \
     --uniform-bucket-level-access
   gcloud storage buckets update gs://loete-tofu-state --versioning
   ```
3. **Authenticate locally** for the initial apply:
   ```sh
   gcloud auth application-default login
   gcloud config set project PROJECT_ID
   ```
4. **Fill in `production.tfvars`** — at minimum `project_id` and `github_repo`.
5. **First apply:**
   ```sh
   just infra-init
   just infra-apply
   ```
6. **Set the placeholder secrets:**
   ```sh
   echo -n "<long random string>" | gcloud secrets versions add loete-jwt-secret --data-file=-
   echo -n "<ticketmaster-api-key>" | gcloud secrets versions add loete-ticketmaster-api-key --data-file=-
   ```
7. **Capture outputs** to configure GitHub secrets (see below):
   ```sh
   cd infra && tofu output
   ```
8. **Update `cors_origins` in `production.tfvars`** to the deployed frontend URL and re-run `just infra-apply`.

## GitHub repository secrets

Set these in the GitHub repo settings → Secrets and variables → Actions:

| Secret             | Value                                           |
| ------------------ | ----------------------------------------------- |
| `GCP_PROJECT_ID`   | GCP project ID                                  |
| `GCP_WIF_PROVIDER` | `tofu output -raw workload_identity_provider`   |
| `GCP_SA_EMAIL`     | `tofu output -raw github_service_account_email` |

The workflow uses Workload Identity Federation — no service-account JSON keys are required or stored anywhere.

## Workflow

Every push and PR runs `checks` and `infra-checks`. On merge to `main`:

1. `build-push` — builds and pushes backend + frontend Docker images to Artifact Registry.
2. `deploy` — rolls out both Cloud Run services to the new image, then runs health checks.
3. `infra` — applies any Tofu changes after a successful deploy.

Flyway migrations run automatically on backend startup, so no separate migrate job is needed.

## Local recipes

```sh
just infra-init        # init backend & providers
just infra-fmt-check   # tofu fmt -check -recursive
just infra-validate    # tofu validate
just infra-plan        # tofu plan
just infra-apply       # tofu apply (prompts)
```
