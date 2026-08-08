package dev.designdeck.api.repository;

import dev.designdeck.api.entity.Favorite;
import dev.designdeck.api.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
  List<Favorite> findByUser_Id(UUID userId);
}
