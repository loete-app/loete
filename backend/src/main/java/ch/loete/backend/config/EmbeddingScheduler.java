package ch.loete.backend.config;

import ch.loete.backend.domain.service.VibeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class EmbeddingScheduler {

  private final VibeSearchService vibeSearchService;

  @Scheduled(cron = "0 30 3 * * *", zone = "Europe/Zurich")
  public void nightlyEmbedding() {
    log.info("Starting nightly embedding run");
    try {
      vibeSearchService.embedPendingEvents();
    } catch (Exception e) {
      log.error("Nightly embedding failed: {}", e.getMessage(), e);
    }
  }

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
