package dev.designdeck.api.repository;

import dev.designdeck.api.entity.Favorite;
import dev.designdeck.api.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {}
