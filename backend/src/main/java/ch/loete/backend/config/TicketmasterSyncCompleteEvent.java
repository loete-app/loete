package ch.loete.backend.config;

import org.springframework.context.ApplicationEvent;

/**
 * Anwendungsereignis, das nach dem erfolgreichen Abschluss einer Ticketmaster-Synchronisation
 * publiziert wird.
 *
 * <p>Wird von {@link TicketmasterSyncScheduler} ausgelöst und von {@link EmbeddingScheduler}
 * konsumiert, um anschliessend die Embedding-Generierung zu starten.
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
