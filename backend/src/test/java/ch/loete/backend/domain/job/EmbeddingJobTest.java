package ch.loete.backend.domain.job;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.loete.backend.domain.service.VibeSearchService;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmbeddingJobTest {

  @Mock private VibeSearchService vibeSearchService;

  @InjectMocks private EmbeddingJob job;

  @Test
  void runEmbedding_callsService() {
    job.runEmbedding();

    verify(vibeSearchService).embedPendingEvents();
  }

  @Test
  void runEmbedding_swallowsExceptions() {
    doThrow(new RuntimeException("boom")).when(vibeSearchService).embedPendingEvents();

    job.runEmbedding();

    verify(vibeSearchService).embedPendingEvents();
  }

  @Test
  void runEmbedding_skipsWhenAlreadyRunning() throws Exception {
    setRunning(true);

    job.runEmbedding();

    verify(vibeSearchService, never()).embedPendingEvents();
  }

  @Test
  void runEmbedding_resetsRunningFlagOnSuccess() {
    job.runEmbedding();
    job.runEmbedding();

    verify(vibeSearchService, times(2)).embedPendingEvents();
  }

  @Test
  void onSyncComplete_callsService() {
    job.onSyncComplete(new TicketmasterSyncCompleteEvent(this));

    verify(vibeSearchService).embedPendingEvents();
  }

  @Test
  void onSyncComplete_swallowsExceptions() {
    doThrow(new RuntimeException("boom")).when(vibeSearchService).embedPendingEvents();

    job.onSyncComplete(new TicketmasterSyncCompleteEvent(this));

    verify(vibeSearchService).embedPendingEvents();
  }

  private void setRunning(boolean value) throws Exception {
    Field field = EmbeddingJob.class.getDeclaredField("running");
    field.setAccessible(true);
    ((AtomicBoolean) field.get(job)).set(value);
  }
}
