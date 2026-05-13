package ch.loete.backend.domain.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.loete.backend.domain.service.TicketmasterIntegrationService;
import ch.loete.backend.process.repository.EventRepository;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TicketmasterSyncJobTest {

  @Mock private TicketmasterIntegrationService integrationService;
  @Mock private EventRepository eventRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private TicketmasterSyncJob job;

  @Test
  void runSync_callsServiceAndPublishesEvent() {
    job.runSync();

    verify(integrationService).syncUpcomingEvents();
    verify(eventPublisher).publishEvent(any(TicketmasterSyncCompleteEvent.class));
  }

  @Test
  void runSync_swallowsExceptionsAndDoesNotPublish() {
    given(integrationService.syncUpcomingEvents()).willThrow(new RuntimeException("boom"));

    job.runSync();

    verify(integrationService).syncUpcomingEvents();
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void runSync_skipsWhenAlreadyRunning() throws Exception {
    setRunning(true);

    job.runSync();

    verify(integrationService, never()).syncUpcomingEvents();
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void runSync_resetsRunningFlagOnSuccess() {
    job.runSync();
    job.runSync();

    verify(integrationService, times(2)).syncUpcomingEvents();
  }

  @Test
  void run_skipsBootstrapWhenEventsAlreadyPresent() {
    given(eventRepository.count()).willReturn(5L);

    job.run(new DefaultApplicationArguments());

    verify(integrationService, never()).syncUpcomingEvents();
    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void run_runsBootstrapWhenEventsEmpty() {
    given(eventRepository.count()).willReturn(0L);

    job.run(new DefaultApplicationArguments());

    verify(integrationService).syncUpcomingEvents();
    verify(eventPublisher).publishEvent(any(TicketmasterSyncCompleteEvent.class));
  }

  @Test
  void run_swallowsBootstrapExceptions() {
    given(eventRepository.count()).willReturn(0L);
    given(integrationService.syncUpcomingEvents()).willThrow(new RuntimeException("kapow"));

    job.run(new DefaultApplicationArguments());

    verify(eventPublisher, never()).publishEvent(any());
  }

  private void setRunning(boolean value) throws Exception {
    Field field = TicketmasterSyncJob.class.getDeclaredField("running");
    field.setAccessible(true);
    ((AtomicBoolean) field.get(job)).set(value);
  }
}
