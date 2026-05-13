package ch.loete.backend.domain.job;

import ch.loete.backend.domain.service.VibeSearchService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Job für die Generierung von Event-Embeddings.
 *
 * <p>Wird extern via Cloud Scheduler über {@code POST /api/internal/jobs/embeddings} angestossen
 * und reagiert zusätzlich auf {@link TicketmasterSyncCompleteEvent}, um direkt nach einer
 * Synchronisation Embeddings für neu importierte Events zu generieren.
 */
@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class EmbeddingJob {

  /** Service für die Vibe-Suche und Embedding-Generierung. */
  private final VibeSearchService vibeSearchService;

  /** Verhindert überlappende Ausführungen auf derselben Instanz. */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Führt die Embedding-Generierung asynchron im {@code jobExecutor} aus. Wird vom {@code
   * InternalJobsController} aufgerufen.
   */
  @Async("jobExecutor")
  public void runEmbedding() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Embedding job already running, skipping concurrent invocation");
      return;
    }
    try {
      log.info("Starting nightly embedding run");
      vibeSearchService.embedPendingEvents();
    } catch (Exception e) {
      log.error("Nightly embedding failed: {}", e.getMessage(), e);
    } finally {
      running.set(false);
    }
  }

  /**
   * Reagiert auf das Abschlussereignis der Ticketmaster-Synchronisation und startet anschliessend
   * die Embedding-Generierung für neu importierte Events.
   *
   * @param event das Synchronisations-Abschlussereignis
   */
  @EventListener
  public void onSyncComplete(TicketmasterSyncCompleteEvent event) {
    log.info("Ticketmaster sync complete, embedding pending events");
    try {
      vibeSearchService.embedPendingEvents();
    } catch (Exception e) {
      log.error("Post-sync embedding failed: {}", e.getMessage(), e);
    }
  }
}
