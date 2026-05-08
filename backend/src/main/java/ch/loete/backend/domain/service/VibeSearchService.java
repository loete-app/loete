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

/**
 * Service für die semantische "Vibe-Suche" nach Events.
 *
 * <p>Kombiniert pgvector-basierte semantische Suche (Embedding-Ähnlichkeit) mit keyword-basierter
 * Textsuche. Semantische Ergebnisse werden priorisiert, Keyword-Treffer füllen auf. Verwaltet
 * ausserdem die Batch-Generierung von Embeddings für Events ohne oder mit veralteten Embeddings.
 */
@Slf4j
@Service
public class VibeSearchService {

  /** Repository für den Zugriff auf Event-Daten und native pgvector-Queries. */
  private final EventRepository eventRepository;

  /** Service für paginierte Event-Abfragen (Keyword-Fallback). */
  private final EventService eventService;

  /** Service für die Generierung von Vektor-Embeddings. */
  private final EmbeddingService embeddingService;

  /** Builder für die textuelle Embedding-Eingabe. */
  private final EmbeddingInputBuilder embeddingInputBuilder;

  /** Flag, ob die Vibe-Suche aktiviert ist. */
  @Value("${app.vibe-search.enabled}")
  private boolean vibeSearchEnabled;

  /** Schwellenwert für die Kosinusaehnlichkeit (0-1, niedriger = strenger). */
  @Value("${app.vibe-search.similarity-threshold}")
  private double similarityThreshold;

  /** Batch-Grösse für die Embedding-Generierung. */
  @Value("${app.vibe-search.embedding-batch-size}")
  private int embeddingBatchSize;

  /** Standard-Limit für Suchergebnisse. */
  @Value("${app.vibe-search.default-limit}")
  private int defaultLimit;

  /**
   * Erstellt einen neuen VibeSearchService.
   *
   * @param eventRepository Repository für Event-Daten
   * @param eventService Service für Event-Abfragen
   * @param embeddingService Service für Embedding-Generierung
   * @param embeddingInputBuilder Builder für Embedding-Eingabetexte
   */
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

  /**
   * Führt eine hybride Vibe-Suche durch (semantisch + keyword).
   *
   * <p>Generiert ein Embedding für die Suchanfrage und findet ähnliche Events via pgvector. Ergänzt
   * die Ergebnisse mit Keyword-Treffern, um auch direkte Namenssuchen abzudecken.
   *
   * @param request die Suchanfrage mit Query, Filtern und Limit
   * @return die Suchergebnisse
   */
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

    List<EventResponse> keywordResults = getKeywordResults(request, limit);

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

  /**
   * Generiert Embeddings für Events, die noch keines besitzen oder deren Embedding älter als 7 Tage
   * ist.
   *
   * <p>Verarbeitet Events in Batches und haelt zwischen Batches eine kurze Pause ein, um die API
   * nicht zu ueberlasten.
   */
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

  /**
   * Holt Keyword-basierte Suchergebnisse als Fallback.
   *
   * @param request die Suchanfrage
   * @param limit maximale Anzahl Ergebnisse
   * @return die Keyword-Treffer
   */
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

  /**
   * Prüft, ob die Suchanfrage strukturierte Filter (Kategorie, Stadt, Datum) enthält.
   *
   * @param request die Suchanfrage
   * @return {@code true} wenn mindestens ein Filter gesetzt ist
   */
  private boolean hasStructuredFilters(VibeSearchRequest request) {
    return request.categoryId() != null
        || request.city() != null
        || request.dateFrom() != null
        || request.dateTo() != null;
  }

  /**
   * Konvertiert eine Event-Entität in ein EventResponse-DTO.
   *
   * @param event die Event-Entität
   * @return das Response-DTO
   */
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
