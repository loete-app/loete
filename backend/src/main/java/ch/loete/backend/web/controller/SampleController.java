package ch.loete.backend.web.controller;

import ch.loete.backend.domain.enums.ItemCategory;
import ch.loete.backend.domain.enums.Priority;
import ch.loete.backend.domain.service.SampleItemService;
import ch.loete.backend.web.dto.request.SampleCreateRequest;
import ch.loete.backend.web.dto.response.SampleItemResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sample")
public class SampleController {

  private final SampleItemService service;

  public SampleController(SampleItemService service) {
    this.service = service;
  }

  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping("/items")
  public List<SampleItemResponse> list(
      @RequestParam(required = false) ItemCategory category,
      @RequestParam(required = false) Priority priority) {
    return service.findAll(category, priority);
  }

  @GetMapping("/items/{id}")
  public SampleItemResponse get(@PathVariable String id) {
    return service.findById(id);
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public SampleItemResponse create(@Valid @RequestBody SampleCreateRequest request) {
    return service.create(request);
  }

  @DeleteMapping("/items/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    service.delete(id);
  }
}
