package dev.designdeck.api.service.impl;

import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.Favorite;
import dev.designdeck.api.entity.FavoriteId;
import dev.designdeck.api.entity.Question;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.mapper.CatalogMapper;
import dev.designdeck.api.repository.AppUserRepository;
import dev.designdeck.api.repository.FavoriteRepository;
import dev.designdeck.api.repository.QuestionRepository;
import dev.designdeck.api.service.FavoritesService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FavoritesServiceImpl implements FavoritesService {

  private final FavoriteRepository favoriteRepository;
  private final AppUserRepository appUserRepository;
  private final QuestionRepository questionRepository;
  private final CatalogMapper catalogMapper;

  public FavoritesServiceImpl(
      FavoriteRepository favoriteRepository,
      AppUserRepository appUserRepository,
      QuestionRepository questionRepository,
      CatalogMapper catalogMapper) {
    this.favoriteRepository = favoriteRepository;
    this.appUserRepository = appUserRepository;
    this.questionRepository = questionRepository;
    this.catalogMapper = catalogMapper;
  }

  @Override
  public boolean toggle(UUID userId, UUID questionId) {
    FavoriteId id = new FavoriteId(userId, questionId);
    if (favoriteRepository.existsById(id)) {
      favoriteRepository.deleteById(id);
      return false; // removed
    }
    AppUser user = appUserRepository.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
    favoriteRepository.save(new Favorite(user, question));
    return true; // added
  }

  @Override
  @Transactional(readOnly = true)
  public List<QuestionDto> list(UUID userId) {
    return favoriteRepository.findByUser_Id(userId).stream()
        .map(Favorite::getQuestion)
        .map(catalogMapper::toQuestionDto)
        .toList();
  }
}
