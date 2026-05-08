package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-Repository für den Datenbankzugriff auf {@link Event}-Entitäten.
 *
 * <p>Bietet neben Standard-CRUD-Operationen auch native pgvector-Queries für die semantische
 * Ähnlichkeitssuche und Embedding-Verwaltung.
 */
@Repository
public interface EventRepository
    extends JpaRepository<Event, String>, JpaSpecificationExecutor<Event> {

  /**
   * Findet ein Event anhand seiner externen Ticketmaster-ID.
   *
   * @param externalId die externe ID
   * @return das Event, falls vorhanden
   */
  Optional<Event> findByExternalId(String externalId);

  /**
   * Löscht alle Events, deren Startdatum vor dem angegebenen Zeitpunkt liegt.
   *
   * @param cutoff der Grenz-Zeitpunkt
   * @return die Anzahl geloeschter Events
   */
  @Modifying
  @Transactional
  int deleteByStartDateBefore(LocalDateTime cutoff);

  /**
   * Findet Events anhand der Kosinusaehnlichkeit ihres Embeddings zum Query-Vektor.
   *
   * @param queryVector der Query-Vektor als pgvector-String
   * @param threshold der Ähnlichkeitsschwellenwert
   * @param limit maximale Anzahl Ergebnisse
   * @return die ähnlichsten Events, sortiert nach Ähnlichkeit
   */
  @Query(
      value =
          "SELECT e.* FROM events e"
              + " WHERE e.embedding IS NOT NULL"
              + " AND (e.embedding <=> CAST(:queryVector AS vector)) < :threshold"
              + " ORDER BY (e.embedding <=> CAST(:queryVector AS vector)) ASC"
              + " LIMIT :limit",
      nativeQuery = true)
  List<Event> findBySimilarity(
      @Param("queryVector") String queryVector,
      @Param("threshold") double threshold,
      @Param("limit") int limit);

  /**
   * Findet Events mittels hybrider Suche: Kosinusaehnlichkeit kombiniert mit optionalen
   * strukturierten Filtern (Kategorie, Stadt, Datumsbereich).
   *
   * @param queryVector der Query-Vektor als pgvector-String
   * @param threshold der Ähnlichkeitsschwellenwert
   * @param categoryId Kategorie-ID-Filter (oder {@code null})
   * @param city Stadt-Filter (oder {@code null})
   * @param dateFrom Startdatum-Filter (oder {@code null})
   * @param dateTo Enddatum-Filter (oder {@code null})
   * @param limit maximale Anzahl Ergebnisse
   * @return die gefilterten, ähnlichsten Events
   */
  @Query(
      value =
          "SELECT e.* FROM events e"
              + " LEFT JOIN categories c ON c.id = e.category_id"
              + " LEFT JOIN locations l ON l.id = e.location_id"
              + " WHERE e.embedding IS NOT NULL"
              + " AND (e.embedding <=> CAST(:queryVector AS vector)) < :threshold"
              + " AND (:categoryId IS NULL OR c.id = :categoryId)"
              + " AND (CAST(:city AS TEXT) IS NULL OR LOWER(l.city) ="
              + " LOWER(CAST(:city AS TEXT)))"
              + " AND (CAST(:dateFrom AS TIMESTAMP) IS NULL OR e.start_date >="
              + " CAST(:dateFrom AS TIMESTAMP))"
              + " AND (CAST(:dateTo AS TIMESTAMP) IS NULL OR e.start_date <="
              + " CAST(:dateTo AS TIMESTAMP))"
              + " ORDER BY (e.embedding <=> CAST(:queryVector AS vector)) ASC"
              + " LIMIT :limit",
      nativeQuery = true)
  List<Event> findByHybridSearch(
      @Param("queryVector") String queryVector,
      @Param("threshold") double threshold,
      @Param("categoryId") Long categoryId,
      @Param("city") String city,
      @Param("dateFrom") LocalDateTime dateFrom,
      @Param("dateTo") LocalDateTime dateTo,
      @Param("limit") int limit);

  /**
   * Aktualisiert das Embedding eines Events via nativem SQL.
   *
   * @param eventId die Event-ID
   * @param embedding der Embedding-Vektor als pgvector-String
   * @param embeddingInput der Eingabetext, der für das Embedding verwendet wurde
   */
  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE events SET embedding = CAST(:embedding AS vector),"
              + " embedding_input = :embeddingInput,"
              + " embedded_at = NOW()"
              + " WHERE id = :eventId",
      nativeQuery = true)
  void updateEmbedding(
      @Param("eventId") String eventId,
      @Param("embedding") String embedding,
      @Param("embeddingInput") String embeddingInput);

  /**
   * Findet Events, die ein Embedding benötigen (kein Embedding oder älter als der Cutoff).
   *
   * @param staleCutoff der Zeitpunkt, ab dem Embeddings als veraltet gelten
   * @return die Events, sortiert nach Embedding-Alter (älteste zuerst)
   */
  @Query(
      "SELECT e FROM Event e"
          + " LEFT JOIN FETCH e.category"
          + " LEFT JOIN FETCH e.location"
          + " WHERE e.embeddedAt IS NULL"
          + " OR e.embeddedAt < :staleCutoff"
          + " ORDER BY e.embeddedAt ASC NULLS FIRST")
  List<Event> findEventsNeedingEmbedding(@Param("staleCutoff") java.time.Instant staleCutoff);
}
