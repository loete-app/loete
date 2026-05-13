package ch.loete.backend.domain.job;

import ch.loete.backend.domain.service.TicketmasterIntegrationService;
import ch.loete.backend.process.repository.EventRepository;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Job für die Synchronisation von Events aus der Ticketmaster-API.
 *
 * <p>Wird extern via Cloud Scheduler über {@code POST /api/internal/jobs/ticketmaster-sync}
 * angestossen und führt zusätzlich beim Anwendungsstart eine initiale Synchronisation durch, falls
 * die Events-Tabelle leer ist. Nach jeder erfolgreichen Synchronisation wird ein {@link
 * TicketmasterSyncCompleteEvent} publiziert.
 */
@Slf4j
@Component
@Profile("!test & !testdata")
@RequiredArgsConstructor
public class TicketmasterSyncJob implements ApplicationRunner {

  /** Service für die Ticketmaster-Integration und den Event-Import. */
  private final TicketmasterIntegrationService integrationService;

  /** Repository für den Zugriff auf Event-Daten. */
  private final EventRepository eventRepository;

  /** Publisher für Anwendungsereignisse. */
  private final ApplicationEventPublisher eventPublisher;

  /** Verhindert überlappende Ausführungen auf derselben Instanz. */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Führt die Ticketmaster-Synchronisation asynchron im {@code jobExecutor} aus. Wird vom {@code
   * InternalJobsController} aufgerufen.
   */
  @Async("jobExecutor")
  public void runSync() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Ticketmaster sync already running, skipping concurrent invocation");
      return;
    }
    try {
      log.info("Starting scheduled Ticketmaster sync");
      integrationService.syncUpcomingEvents();
      eventPublisher.publishEvent(new TicketmasterSyncCompleteEvent(this));
    } catch (Exception e) {
      log.error("Scheduled Ticketmaster sync failed: {}", e.getMessage(), e);
    } finally {
      running.set(false);
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
