package ch.loete.backend.process.repository;

import ch.loete.backend.domain.model.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByNameIgnoreCase(String name);

  List<Category> findAllByOrderByNameAsc();
}
