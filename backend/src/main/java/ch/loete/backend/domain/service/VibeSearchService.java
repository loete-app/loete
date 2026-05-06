package ch.loete.backend.domain.service;

import ch.loete.backend.domain.model.Event;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.request.VibeSearchRequest;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.VibeSearchResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class VibeSearchService {

  private final EventRepository eventRepository;
  private final EventService eventService;
  private final EmbeddingService embeddingService;
  private final EmbeddingInputBuilder embeddingInputBuilder;

  @Value("${app.vibe-search.enabled}")
  private boolean vibeSearchEnabled;

  @Value("${app.vibe-search.similarity-threshold}")
  private double similarityThreshold;

  @Value("${app.vibe-search.embedding-batch-size}")
  private int embeddingBatchSize;

  @Value("${app.vibe-search.default-limit}")
  private int defaultLimit;

  public VibeSearchService(
      EventRepository eventRepository,
      EventService eventService,
      EmbeddingService embeddingService,
      EmbeddingInputBuilder embeddingInputBuilder) {
    this.eventRepository = eventRepository;
    this.eventService = eventService;
    this.embeddingService = embeddingService;
    this.embeddingInputBuilder = embeddingInputBuilder;
  }

  @Transactional(readOnly = true)
  public VibeSearchResponse search(VibeSearchRequest request) {
    int limit = request.limit() != null ? request.limit() : defaultLimit;

    List<Event> semanticResults = List.of();
    if (vibeSearchEnabled && embeddingService.isConfigured()) {
      try {
        float[] queryEmbedding = embeddingService.generateEmbedding(request.query());
        String vectorString = EmbeddingService.toVectorString(queryEmbedding);

        if (hasStructuredFilters(request)) {
          semanticResults =
              eventRepository.findByHybridSearch(
                  vectorString,
                  similarityThreshold,
                  request.categoryId(),
                  request.city(),
                  request.dateFrom(),
                  request.dateTo(),
                  limit);
        } else {
          semanticResults =
              eventRepository.findBySimilarity(vectorString, similarityThreshold, limit);
        }
      } catch (Exception e) {
        log.error("Semantic search failed, backfilling with keyword results: {}", e.getMessage());
      }
    }

    // Always backfill with keyword results to cover direct name searches
    List<EventResponse> keywordResults = getKeywordResults(request, limit);

    // Merge: semantic first (ranked by relevance), then keyword hits not already present
    Set<String> seenIds = new LinkedHashSet<>();
    List<EventResponse> merged = new ArrayList<>();

    for (Event event : semanticResults) {
      if (seenIds.add(event.getId())) {
        merged.add(toEventResponse(event));
      }
    }
    for (EventResponse event : keywordResults) {
      if (seenIds.add(event.id())) {
        merged.add(event);
      }
    }

    List<EventResponse> results = merged.size() > limit ? merged.subList(0, limit) : merged;
    return new VibeSearchResponse(results, false);
  }

  public void embedPendingEvents() {
    if (!vibeSearchEnabled || !embeddingService.isConfigured()) {
      log.debug("Embedding skipped: vibe search disabled or API key not configured");
      return;
    }

    Instant staleCutoff = Instant.now().minus(7, ChronoUnit.DAYS);
    List<Event> pending = eventRepository.findEventsNeedingEmbedding(staleCutoff);

    if (pending.isEmpty()) {
      log.info("No events need embedding");
      return;
    }

    log.info("Embedding {} events", pending.size());

    for (int i = 0; i < pending.size(); i += embeddingBatchSize) {
      int end = Math.min(i + embeddingBatchSize, pending.size());
      List<Event> batch = pending.subList(i, end);

      List<String> inputs = batch.stream().map(embeddingInputBuilder::buildEmbeddingInput).toList();

      try {
        List<float[]> embeddings = embeddingService.generateEmbeddings(inputs);

        for (int j = 0; j < batch.size(); j++) {
          Event event = batch.get(j);
          String vectorString = EmbeddingService.toVectorString(embeddings.get(j));
          eventRepository.updateEmbedding(event.getId(), vectorString, inputs.get(j));
        }

        log.info("Embedded batch {}-{} of {}", i + 1, end, pending.size());

        if (end < pending.size()) {
          Thread.sleep(500);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Embedding interrupted at batch {}-{}", i + 1, end);
        break;
      } catch (Exception e) {
        log.error("Failed to embed batch {}-{}: {}", i + 1, end, e.getMessage());
      }
    }

    log.info("Embedding complete");
  }

  private List<EventResponse> getKeywordResults(VibeSearchRequest request, int limit) {
    EventFilterRequest filter =
        new EventFilterRequest(
            request.categoryId(),
            request.city(),
            request.dateFrom(),
            request.dateTo(),
            request.query(),
            0,
            limit);
    return eventService.getEvents(filter).content();
  }

  private boolean hasStructuredFilters(VibeSearchRequest request) {
    return request.categoryId() != null
        || request.city() != null
        || request.dateFrom() != null
        || request.dateTo() != null;
  }

  private EventResponse toEventResponse(Event event) {
    return new EventResponse(
        event.getId(),
        event.getName(),
        event.getImageUrl(),
        event.getStartDate(),
        event.getCategory() != null ? event.getCategory().getName() : null,
        event.getLocation() != null ? event.getLocation().getName() : null,
        event.getLocation() != null ? event.getLocation().getCity() : null);
  }
}
