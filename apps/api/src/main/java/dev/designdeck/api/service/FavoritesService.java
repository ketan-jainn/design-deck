package dev.designdeck.api.service;

import dev.designdeck.api.dto.catalog.QuestionDto;
import java.util.List;
import java.util.UUID;

public interface FavoritesService {
  /** Adds the question to favorites if not already present; removes it if it is. */
  boolean toggle(UUID userId, UUID questionId);
  /** Returns all questions favorited by the user. */
  List<QuestionDto> list(UUID userId);
}
