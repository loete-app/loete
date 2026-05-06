package ch.loete.backend.config;

import org.springframework.context.ApplicationEvent;

public class TicketmasterSyncCompleteEvent extends ApplicationEvent {

  public TicketmasterSyncCompleteEvent(Object source) {
    super(source);
  }
}
