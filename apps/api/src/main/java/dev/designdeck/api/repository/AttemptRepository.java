package dev.designdeck.api.repository;

import dev.designdeck.api.entity.Attempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
  @Query(
      value = """
          select count(*) from attempts
          where user_id = :userId and created_at >= date_trunc('day', now())
          """,
      nativeQuery = true)
  long countTodayAttempts(@Param("userId") UUID userId);
}
