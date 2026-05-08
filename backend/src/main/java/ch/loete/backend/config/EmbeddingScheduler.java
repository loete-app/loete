package ch.loete.backend.config;

import ch.loete.backend.domain.service.VibeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler für die Generierung von Event-Embeddings.
 *
 * <p>Führt nächtlich und nach jeder Ticketmaster-Synchronisation die Embedding-Generierung für
 * Events durch, die noch kein Embedding besitzen oder deren Embedding veraltet ist.
 */
@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class EmbeddingScheduler {

  /** Service für die Vibe-Suche und Embedding-Generierung. */
  private final VibeSearchService vibeSearchService;

  /** Führt die nächtliche Embedding-Generierung um 03:30 Uhr (Europe/Zurich) aus. */
  @Scheduled(cron = "0 30 3 * * *", zone = "Europe/Zurich")
  public void nightlyEmbedding() {
    log.info("Starting nightly embedding run");
    try {
      vibeSearchService.embedPendingEvents();
    } catch (Exception e) {
      log.error("Nightly embedding failed: {}", e.getMessage(), e);
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
