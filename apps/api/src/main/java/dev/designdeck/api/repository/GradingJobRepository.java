package dev.designdeck.api.repository;

import dev.designdeck.api.entity.GradingJob;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradingJobRepository extends JpaRepository<GradingJob, UUID> {
  Optional<GradingJob> findByIdAndUser_Id(UUID id, UUID userId);
}
