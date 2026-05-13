package ch.loete.backend.web.controller;

import ch.loete.backend.domain.job.EmbeddingJob;
import ch.loete.backend.domain.job.TicketmasterSyncJob;
import java.util.concurrent.RejectedExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Controller für extern via Cloud Scheduler angestossene Cron-Jobs.
 *
 * <p>Beide Endpunkte stossen die jeweilige Aufgabe asynchron im {@code jobExecutor} an und
 * antworten sofort mit {@code 202 Accepted}, damit Cloud Scheduler keine HTTP-Verbindung über die
 * Cloud-Run-Request-Timeout-Grenze hinaus offenhalten muss.
 *
 * <p>Ist der single-thread {@code jobExecutor} bereits ausgelastet (z. B. weil der laufende
 * Ticketmaster-Sync via Event-Listener gerade Embeddings generiert), lehnt er die zweite Einreihung
 * mit einer {@link RejectedExecutionException} ab. Diese wird hier als Skip behandelt — wir
 * antworten Cloud Scheduler trotzdem mit {@code 202}, damit der Job nicht als fehlgeschlagen
 * markiert wird, und protokollieren den Skip für die Beobachtbarkeit.
 *
 * <p>Authentifizierung erfolgt im Prod-Profil über die {@code /internal/**}-Filter-Kette via
 * Google-OIDC; in anderen Profilen sind die Endpunkte offen.
 */
@Slf4j
@RestController
@RequestMapping("/internal/jobs")
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class InternalJobsController {

  /** Job für die Ticketmaster-Event-Synchronisation. */
  private final TicketmasterSyncJob ticketmasterSyncJob;

  /** Job für die Embedding-Generierung. */
  private final EmbeddingJob embeddingJob;

  /**
   * Stösst eine Ticketmaster-Synchronisation an.
   *
   * @return {@code 202 Accepted}, sowohl bei erfolgreicher Einreihung als auch bei Skip wegen
   *     ausgelastetem Executor
   */
  @PostMapping("/ticketmaster-sync")
  public ResponseEntity<Void> triggerTicketmasterSync() {
    try {
      ticketmasterSyncJob.runSync();
    } catch (RejectedExecutionException e) {
      log.warn("Ticketmaster sync skipped: jobExecutor busy with another job");
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  /**
   * Stösst die Embedding-Generierung an.
   *
   * @return {@code 202 Accepted}, sowohl bei erfolgreicher Einreihung als auch bei Skip wegen
   *     ausgelastetem Executor
   */
  @PostMapping("/embeddings")
  public ResponseEntity<Void> triggerEmbeddings() {
    try {
      embeddingJob.runEmbedding();
    } catch (RejectedExecutionException e) {
      log.warn("Embedding job skipped: jobExecutor busy with another job");
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
