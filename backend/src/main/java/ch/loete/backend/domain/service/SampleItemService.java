package ch.loete.backend.domain.service;

import ch.loete.backend.domain.enums.ItemCategory;
import ch.loete.backend.domain.enums.Priority;
import ch.loete.backend.domain.exception.DuplicateResourceException;
import ch.loete.backend.domain.exception.ResourceNotFoundException;
import ch.loete.backend.web.dto.request.SampleCreateRequest;
import ch.loete.backend.web.dto.response.SampleItemResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class SampleItemService {

  private final List<SampleItemResponse> items =
      new CopyOnWriteArrayList<>(
          List.of(
              new SampleItemResponse(
                  "a1b2c3d4", "Wireless Keyboard", "Bluetooth, compact layout",
                  ItemCategory.ELECTRONICS, Priority.MEDIUM, 49.90, Instant.now()),
              new SampleItemResponse(
                  "e5f6g7h8", "Spring Boot in Action", "Manning Publications, 2nd edition",
                  ItemCategory.BOOK, Priority.LOW, 39.00, Instant.now()),
              new SampleItemResponse(
                  "i9j0k1l2", "Winter Jacket", "Waterproof, size M",
                  ItemCategory.CLOTHING, Priority.HIGH, 129.00, Instant.now())));

  public List<SampleItemResponse> findAll(ItemCategory category, Priority priority) {
    return items.stream()
        .filter(i -> category == null || i.category() == category)
        .filter(i -> priority == null || i.priority() == priority)
        .toList();
  }

  public SampleItemResponse findById(String id) {
    return items.stream()
        .filter(i -> i.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException("Item", id));
  }

  public SampleItemResponse create(SampleCreateRequest request) {
    items.stream()
        .filter(i -> i.title().equalsIgnoreCase(request.title()))
        .findFirst()
        .ifPresent(
            i -> {
              throw new DuplicateResourceException("Item", "title", request.title());
            });

    var item =
        new SampleItemResponse(
            UUID.randomUUID().toString().substring(0, 8),
            request.title(),
            request.description(),
            request.category(),
            request.priority(),
            request.price(),
            Instant.now());
    items.add(item);
    return item;
  }

  public void delete(String id) {
    var item = findById(id);
    items.remove(item);
  }
}
