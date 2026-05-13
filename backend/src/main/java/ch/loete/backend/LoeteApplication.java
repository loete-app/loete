package ch.loete.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hauptklasse der Löte-Anwendung.
 *
 * <p>Startet die Spring-Boot-Applikation. Periodische Synchronisations- und Embedding-Jobs werden
 * extern via Cloud Scheduler angestossen (siehe {@code InternalJobsController}).
 */
@SpringBootApplication
public class LoeteApplication {

  /**
   * Einstiegspunkt der Applikation.
   *
   * @param args Kommandozeilenargumente
   */
  public static void main(String[] args) {
    SpringApplication.run(LoeteApplication.class, args);
  }
}
