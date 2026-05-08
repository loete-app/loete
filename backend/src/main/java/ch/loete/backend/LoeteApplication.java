package ch.loete.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hauptklasse der Löte-Anwendung.
 *
 * <p>Startet die Spring-Boot-Applikation und aktiviert die zeitgesteuerte Aufgabenplanung
 * (Scheduling) für periodische Synchronisations- und Embedding-Jobs.
 */
@SpringBootApplication
@EnableScheduling
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
