package ch.loete.backend.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.loete.backend.config.JwtTokenProvider;
import ch.loete.backend.domain.job.EmbeddingJob;
import ch.loete.backend.domain.job.TicketmasterSyncJob;
import ch.loete.backend.process.repository.UserRepository;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalJobsController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalJobsControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TicketmasterSyncJob ticketmasterSyncJob;

  @MockitoBean private EmbeddingJob embeddingJob;

  // Satisfy JwtAuthenticationFilter's dependencies — this test runs without the "test" profile
  // because InternalJobsController itself is profile-guarded against "test"/"testdata".
  @MockitoBean private JwtTokenProvider jwtTokenProvider;

  @MockitoBean private UserDetailsService userDetailsService;

  @MockitoBean private UserRepository userRepository;

  @Test
  void triggerTicketmasterSync_returns202AndInvokesJob() throws Exception {
    mockMvc.perform(post("/internal/jobs/ticketmaster-sync")).andExpect(status().isAccepted());

    Mockito.verify(ticketmasterSyncJob).runSync();
  }

  @Test
  void triggerEmbeddings_returns202AndInvokesJob() throws Exception {
    mockMvc.perform(post("/internal/jobs/embeddings")).andExpect(status().isAccepted());

    Mockito.verify(embeddingJob).runEmbedding();
  }

  @Test
  void triggerTicketmasterSync_returns202WhenExecutorRejects() throws Exception {
    Mockito.doThrow(new RejectedExecutionException("busy")).when(ticketmasterSyncJob).runSync();

    mockMvc.perform(post("/internal/jobs/ticketmaster-sync")).andExpect(status().isAccepted());
  }

  @Test
  void triggerEmbeddings_returns202WhenExecutorRejects() throws Exception {
    Mockito.doThrow(new RejectedExecutionException("busy")).when(embeddingJob).runEmbedding();

    mockMvc.perform(post("/internal/jobs/embeddings")).andExpect(status().isAccepted());
  }
}
