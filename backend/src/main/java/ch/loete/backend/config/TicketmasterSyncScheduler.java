package ch.loete.backend.config;

import ch.loete.backend.domain.service.TicketmasterIntegrationService;
import ch.loete.backend.process.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class TicketmasterSyncScheduler implements ApplicationRunner {

  private final TicketmasterIntegrationService integrationService;
  private final EventRepository eventRepository;
  private final ApplicationEventPublisher eventPublisher;

  /** Daily sync at 03:00 Europe/Zurich. */
  @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Zurich")
  public void scheduledSync() {
    log.info("Starting scheduled Ticketmaster sync");
    try {
      integrationService.syncUpcomingEvents();
      eventPublisher.publishEvent(new TicketmasterSyncCompleteEvent(this));
    } catch (Exception e) {
      log.error("Scheduled Ticketmaster sync failed: {}", e.getMessage(), e);
    }
  }

  /** On boot, sync immediately if the events table is empty. */
  @Override
  public void run(ApplicationArguments args) {
    if (eventRepository.count() > 0) {
      log.info("Events already present, skipping initial Ticketmaster sync");
      return;
    }
    log.info("Events table empty, running initial Ticketmaster sync");
    try {
      integrationService.syncUpcomingEvents();
      eventPublisher.publishEvent(new TicketmasterSyncCompleteEvent(this));
    } catch (Exception e) {
      log.error("Initial Ticketmaster sync failed: {}", e.getMessage(), e);
    }
  }
}
