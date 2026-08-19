package dev.designdeck.api.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.service.FavoritesService;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

  private final FavoritesService favoritesService;

  public FavoritesController(FavoritesService favoritesService) {
    this.favoritesService = favoritesService;
  }

  /** Toggle a question in/out of the user's favorites. Returns { favorited: true|false } */
  @PostMapping("/{questionId}")
  public Map<String, Boolean> toggle(@PathVariable UUID questionId) {
    boolean added = favoritesService.toggle(SecurityUtils.currentUserId(), questionId);
    return Map.of("favorited", added);
  }

  /** List all favorited questions for the current user. */
  @GetMapping
  public List<QuestionDto> list() {
    return favoritesService.list(SecurityUtils.currentUserId());
  }
}
