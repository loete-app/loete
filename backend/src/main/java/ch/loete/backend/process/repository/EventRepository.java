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

@Repository
public interface EventRepository
    extends JpaRepository<Event, String>, JpaSpecificationExecutor<Event> {

  Optional<Event> findByExternalId(String externalId);

  @Modifying
  @Transactional
  int deleteByStartDateBefore(LocalDateTime cutoff);

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

  @Query(
      "SELECT e FROM Event e"
          + " LEFT JOIN FETCH e.category"
          + " LEFT JOIN FETCH e.location"
          + " WHERE e.embeddedAt IS NULL"
          + " OR e.embeddedAt < :staleCutoff"
          + " ORDER BY e.embeddedAt ASC NULLS FIRST")
  List<Event> findEventsNeedingEmbedding(@Param("staleCutoff") java.time.Instant staleCutoff);
}
