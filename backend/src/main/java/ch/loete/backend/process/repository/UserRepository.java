package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findByEmail(String email);

  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
