package ch.loete.backend.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Konfiguriert den Executor für externe Cron-Jobs (Ticketmaster-Sync, Embedding-Generierung).
 *
 * <p>Der {@code jobExecutor} ist auf einen Thread mit Queue-Kapazität 0 begrenzt: gleichzeitige
 * Aufrufe werden abgelehnt, damit ein Job nicht parallel zu sich selbst läuft.
 */
@Configuration
@EnableAsync
public class JobsAsyncConfig {

  /**
   * Single-Thread-Executor für Cron-Jobs. Eine zweite Anfrage während eines laufenden Jobs wird
   * abgewiesen ({@link ThreadPoolExecutor.AbortPolicy}).
   *
   * @return der konfigurierte Executor
   */
  @Bean(name = "jobExecutor")
  public Executor jobExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(0);
    executor.setThreadNamePrefix("job-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    executor.initialize();
    return executor;
  }
}
