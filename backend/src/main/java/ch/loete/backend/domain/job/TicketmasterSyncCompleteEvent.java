package ch.loete.backend.domain.job;

import org.springframework.context.ApplicationEvent;

/**
 * Anwendungsereignis, das nach dem erfolgreichen Abschluss einer Ticketmaster-Synchronisation
 * publiziert wird.
 *
 * <p>Wird von {@link TicketmasterSyncJob} ausgelöst und von {@link EmbeddingJob} konsumiert, um
 * anschliessend die Embedding-Generierung zu starten.
 */
public class TicketmasterSyncCompleteEvent extends ApplicationEvent {

  /**
   * Erstellt ein neues Synchronisations-Abschlussereignis.
   *
   * @param source die Quelle des Ereignisses
   */
  public TicketmasterSyncCompleteEvent(Object source) {
    super(source);
  }
}
