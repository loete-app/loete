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

/**
 * Scheduler für die periodische Synchronisation von Events aus der Ticketmaster-API.
 *
 * <p>Führt täglich um 03:00 Uhr (Europe/Zurich) eine Synchronisation durch und startet beim
 * Anwendungsstart eine initiale Synchronisation, falls die Events-Tabelle leer ist. Nach jeder
 * erfolgreichen Synchronisation wird ein {@link TicketmasterSyncCompleteEvent} publiziert.
 */
@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class TicketmasterSyncScheduler implements ApplicationRunner {

  /** Service für die Ticketmaster-Integration und den Event-Import. */
  private final TicketmasterIntegrationService integrationService;

  /** Repository für den Zugriff auf Event-Daten. */
  private final EventRepository eventRepository;

  /** Publisher für Anwendungsereignisse. */
  private final ApplicationEventPublisher eventPublisher;

  /** Führt die tägliche Ticketmaster-Synchronisation um 03:00 Uhr (Europe/Zurich) aus. */
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

  /**
   * Führt beim Anwendungsstart eine initiale Synchronisation durch, falls noch keine Events in der
   * Datenbank vorhanden sind.
   *
   * @param args die Anwendungsargumente
   */
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
