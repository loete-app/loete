package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Event;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingInputBuilder {

  public String buildEmbeddingInput(Event event) {
    List<String> parts = new ArrayList<>();

    if (event.getName() != null) {
      parts.add(event.getName());
    }

    if (event.getCategory() != null && event.getCategory().getName() != null) {
      parts.add("Category: " + event.getCategory().getName());
    }

    if (event.getLocation() != null) {
      if (event.getLocation().getName() != null) {
        parts.add("Venue: " + event.getLocation().getName());
      }
      if (event.getLocation().getCity() != null) {
        parts.add("City: " + event.getLocation().getCity());
      }
      if (event.getLocation().getCountry() != null) {
        parts.add("Country: " + event.getLocation().getCountry());
      }
    }

    if (event.getStartDate() != null) {
      var date = event.getStartDate();
      var formatter = DateTimeFormatter.ofPattern("EEEE, MMMM yyyy", Locale.ENGLISH);
      parts.add("Date: " + date.format(formatter));
      parts.add("Season: " + getSeason(date.getMonthValue()));
      parts.add("Time of day: " + getTimeOfDay(date.getHour()));
    }

    if (event.getDescription() != null) {
      String desc = event.getDescription();
      if (desc.length() > 500) {
        desc = desc.substring(0, 500);
      }
      parts.add(desc);
    }

    return String.join(". ", parts);
  }

  private String getSeason(int month) {
    return switch (month) {
      case 12, 1, 2 -> "Winter";
      case 3, 4, 5 -> "Spring";
      case 6, 7, 8 -> "Summer";
      case 9, 10, 11 -> "Autumn";
      default -> "Unknown";
    };
  }

  private String getTimeOfDay(int hour) {
    if (hour < 12) return "morning";
    if (hour < 17) return "afternoon";
    if (hour < 21) return "evening";
    return "night";
  }
}
