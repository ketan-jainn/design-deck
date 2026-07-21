package dev.designdeck.api.repository;

import dev.designdeck.api.entity.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
  @Query("select p from Profile p join fetch p.user where p.userId = :id")
  Optional<Profile> findDetailedById(@Param("id") UUID id);
}
