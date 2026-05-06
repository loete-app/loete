package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Favorite;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, String> {

  List<Favorite> findByUser_IdOrderByCreatedAtDesc(String userId);

  Optional<Favorite> findByUser_IdAndEvent_Id(String userId, String eventId);

  boolean existsByUser_IdAndEvent_Id(String userId, String eventId);

  void deleteByUser_IdAndEvent_Id(String userId, String eventId);

  @Query("SELECT f.event.id FROM Favorite f WHERE f.user.id = :userId")
  Set<String> findEventIdsByUserId(@Param("userId") String userId);
}
