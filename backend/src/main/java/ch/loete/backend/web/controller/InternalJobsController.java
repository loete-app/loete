package ch.loete.backend.web.controller;

import ch.loete.backend.domain.job.EmbeddingJob;
import ch.loete.backend.domain.job.TicketmasterSyncJob;
import lombok.RequiredArgsConstructor;
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
 * <p>Authentifizierung erfolgt im Prod-Profil über die {@code /internal/**}-Filter-Kette via
 * Google-OIDC; in anderen Profilen sind die Endpunkte offen.
 */
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
   * @return {@code 202 Accepted}, sobald der Job in den Executor eingereiht wurde
   */
  @PostMapping("/ticketmaster-sync")
  public ResponseEntity<Void> triggerTicketmasterSync() {
    ticketmasterSyncJob.runSync();
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  /**
   * Stösst die Embedding-Generierung an.
   *
   * @return {@code 202 Accepted}, sobald der Job in den Executor eingereiht wurde
   */
  @PostMapping("/embeddings")
  public ResponseEntity<Void> triggerEmbeddings() {
    embeddingJob.runEmbedding();
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
