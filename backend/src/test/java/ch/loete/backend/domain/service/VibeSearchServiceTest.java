package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ch.loete.backend.domain.model.Category;
import ch.loete.backend.domain.model.Event;
import ch.loete.backend.domain.model.Location;
import ch.loete.backend.process.repository.EventRepository;
import ch.loete.backend.web.dto.request.EventFilterRequest;
import ch.loete.backend.web.dto.request.VibeSearchRequest;
import ch.loete.backend.web.dto.response.EventResponse;
import ch.loete.backend.web.dto.response.PagedResponse;
import ch.loete.backend.web.dto.response.VibeSearchResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VibeSearchServiceTest {

  @Mock private EventRepository eventRepository;
  @Mock private EventService eventService;
  @Mock private EmbeddingService embeddingService;
  @Mock private EmbeddingInputBuilder embeddingInputBuilder;

  @InjectMocks private VibeSearchService vibeSearchService;

  @Test
  void search_combinesSemanticAndKeywordResults() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("chill jazz")).willReturn(new float[] {0.1f});

    Event semanticEvent = buildEvent("ev1", "Jazz Night", "Konzert", "Zurich");
    given(eventRepository.findBySimilarity(any(), eq(0.65), eq(20)))
        .willReturn(List.of(semanticEvent));

    EventResponse keywordEvent =
        new EventResponse("ev2", "Jazz Club", null, LocalDateTime.now(), "Konzert", "Bar", "Bern");
    stubKeywordResults(List.of(keywordEvent));

    VibeSearchRequest request = new VibeSearchRequest("chill jazz", null, null, null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.fallback()).isFalse();
    assertThat(response.results()).hasSize(2);
    assertThat(response.results().get(0).name()).isEqualTo("Jazz Night");
    assertThat(response.results().get(1).name()).isEqualTo("Jazz Club");
  }

  @Test
  void search_deduplicatesResults() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("rock")).willReturn(new float[] {0.1f});

    Event event = buildEvent("ev1", "Rock Night", "Konzert", "Zurich");
    given(eventRepository.findBySimilarity(any(), eq(0.65), eq(20))).willReturn(List.of(event));

    EventResponse sameEvent =
        new EventResponse(
            "ev1", "Rock Night", null, LocalDateTime.now(), "Konzert", "Venue", "Zurich");
    stubKeywordResults(List.of(sameEvent));

    VibeSearchRequest request = new VibeSearchRequest("rock", null, null, null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.results()).hasSize(1);
  }

  @Test
  void search_usesHybridWhenStructuredFiltersPresent() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("rock concert")).willReturn(new float[] {0.1f});

    Event event = buildEvent("ev2", "Rock Night", "Konzert", "Bern");
    given(
            eventRepository.findByHybridSearch(
                any(), eq(0.65), eq(1L), eq("Bern"), any(), any(), eq(20)))
        .willReturn(List.of(event));
    stubKeywordResults(List.of());

    VibeSearchRequest request = new VibeSearchRequest("rock concert", 1L, "Bern", null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.fallback()).isFalse();
    assertThat(response.results()).hasSize(1);
    verify(eventRepository, never()).findBySimilarity(any(), anyDouble(), anyInt());
  }

  @Test
  void search_returnsOnlyKeywordResultsWhenVibeDisabled() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", false);
    ReflectionTestUtils.setField(vibeSearchService, "defaultLimit", 20);

    EventResponse keywordEvent =
        new EventResponse(
            "ev3", "Keyword Event", null, LocalDateTime.now(), "Sport", "Arena", "Basel");
    stubKeywordResults(List.of(keywordEvent));

    VibeSearchRequest request = new VibeSearchRequest("any query", null, null, null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.results()).hasSize(1);
    assertThat(response.results().getFirst().name()).isEqualTo("Keyword Event");
    verify(embeddingService, never()).generateEmbedding(any());
  }

  @Test
  void search_returnsOnlyKeywordResultsWhenEmbeddingNotConfigured() {
    setupDefaults();
    given(embeddingService.isConfigured()).willReturn(false);

    EventResponse keywordEvent =
        new EventResponse("ev4", "Fallback", null, LocalDateTime.now(), null, null, null);
    stubKeywordResults(List.of(keywordEvent));

    VibeSearchRequest request = new VibeSearchRequest("query", null, null, null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.results()).hasSize(1);
    verify(embeddingService, never()).generateEmbedding(any());
  }

  @Test
  void search_respectsCustomLimit() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("test")).willReturn(new float[] {0.1f});
    given(eventRepository.findBySimilarity(any(), eq(0.65), eq(5))).willReturn(List.of());
    stubKeywordResults(List.of(), 5);

    VibeSearchRequest request = new VibeSearchRequest("test", null, null, null, null, 5);
    vibeSearchService.search(request);

    verify(eventRepository).findBySimilarity(any(), eq(0.65), eq(5));
  }

  @Test
  void search_fallsBackToKeywordOnEmbeddingException() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("failing query"))
        .willThrow(new RuntimeException("API error"));

    EventResponse keywordEvent =
        new EventResponse("ev5", "Keyword Hit", null, LocalDateTime.now(), null, null, null);
    stubKeywordResults(List.of(keywordEvent));

    VibeSearchRequest request =
        new VibeSearchRequest("failing query", null, null, null, null, null);
    VibeSearchResponse response = vibeSearchService.search(request);

    assertThat(response.results()).hasSize(1);
    assertThat(response.results().getFirst().name()).isEqualTo("Keyword Hit");
  }

  @Test
  void search_usesDateFromFilter() {
    setupDefaults();

    given(embeddingService.isConfigured()).willReturn(true);
    given(embeddingService.generateEmbedding("summer vibes")).willReturn(new float[] {0.1f});

    LocalDateTime dateFrom = LocalDateTime.of(2026, 6, 1, 0, 0);
    given(
            eventRepository.findByHybridSearch(
                any(), eq(0.65), eq(null), eq(null), eq(dateFrom), eq(null), eq(20)))
        .willReturn(List.of());
    stubKeywordResults(List.of());

    VibeSearchRequest request =
        new VibeSearchRequest("summer vibes", null, null, dateFrom, null, null);
    vibeSearchService.search(request);

    verify(eventRepository)
        .findByHybridSearch(any(), anyDouble(), any(), any(), any(), any(), anyInt());
  }

  @Test
  void embedPendingEvents_skipsWhenDisabled() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", false);

    vibeSearchService.embedPendingEvents();

    verify(eventRepository, never()).findEventsNeedingEmbedding(any());
  }

  @Test
  void embedPendingEvents_skipsWhenNotConfigured() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", true);
    given(embeddingService.isConfigured()).willReturn(false);

    vibeSearchService.embedPendingEvents();

    verify(eventRepository, never()).findEventsNeedingEmbedding(any());
  }

  @Test
  void embedPendingEvents_skipsWhenNoPending() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", true);
    given(embeddingService.isConfigured()).willReturn(true);
    given(eventRepository.findEventsNeedingEmbedding(any())).willReturn(List.of());

    vibeSearchService.embedPendingEvents();

    verify(embeddingService, never()).generateEmbeddings(any());
  }

  @Test
  void embedPendingEvents_embedsBatch() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", true);
    ReflectionTestUtils.setField(vibeSearchService, "embeddingBatchSize", 50);
    given(embeddingService.isConfigured()).willReturn(true);

    Event event = buildEvent("ev6", "Embed Me", "Konzert", "Zurich");
    given(eventRepository.findEventsNeedingEmbedding(any())).willReturn(List.of(event));
    given(embeddingInputBuilder.buildEmbeddingInput(event))
        .willReturn("Embed Me. Category: Konzert");
    given(embeddingService.generateEmbeddings(any())).willReturn(List.of(new float[] {0.1f, 0.2f}));

    vibeSearchService.embedPendingEvents();

    verify(eventRepository).updateEmbedding(eq("ev6"), any(), eq("Embed Me. Category: Konzert"));
  }

  @Test
  void embedPendingEvents_toleratesBatchFailure() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", true);
    ReflectionTestUtils.setField(vibeSearchService, "embeddingBatchSize", 50);
    given(embeddingService.isConfigured()).willReturn(true);

    Event event = buildEvent("ev7", "Fail Event", "Sport", "Bern");
    given(eventRepository.findEventsNeedingEmbedding(any())).willReturn(List.of(event));
    given(embeddingInputBuilder.buildEmbeddingInput(event)).willReturn("Fail Event");
    given(embeddingService.generateEmbeddings(any())).willThrow(new RuntimeException("API down"));

    vibeSearchService.embedPendingEvents();

    verify(eventRepository, never()).updateEmbedding(any(), any(), any());
  }

  private void setupDefaults() {
    ReflectionTestUtils.setField(vibeSearchService, "vibeSearchEnabled", true);
    ReflectionTestUtils.setField(vibeSearchService, "similarityThreshold", 0.65);
    ReflectionTestUtils.setField(vibeSearchService, "defaultLimit", 20);
  }

  private void stubKeywordResults(List<EventResponse> results) {
    stubKeywordResults(results, 20);
  }

  private void stubKeywordResults(List<EventResponse> results, int size) {
    PagedResponse<EventResponse> pagedResponse =
        new PagedResponse<>(results, 0, size, results.size(), 1, true);
    given(eventService.getEvents(any(EventFilterRequest.class))).willReturn(pagedResponse);
  }

  private Event buildEvent(String id, String name, String categoryName, String city) {
    Category category =
        Category.builder().id(1L).name(categoryName).slug(categoryName.toLowerCase()).build();
    Location location = Location.builder().id(1L).name("Venue").city(city).country("CH").build();
    return Event.builder()
        .id(id)
        .name(name)
        .startDate(LocalDateTime.of(2026, 7, 15, 20, 0))
        .category(category)
        .location(location)
        .build();
  }
}
