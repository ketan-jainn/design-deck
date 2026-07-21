package dev.designdeck.api.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.designdeck.api.dto.catalog.CategoryDto;
import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.mapper.CatalogMapper;
import dev.designdeck.api.repository.CategoryRepository;
import dev.designdeck.api.repository.QuestionRepository;
import dev.designdeck.api.service.CatalogService;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {
  private final CategoryRepository categoryRepository;
  private final QuestionRepository questionRepository;
  private final CatalogMapper catalogMapper;

  public CatalogServiceImpl(
      CategoryRepository categoryRepository, QuestionRepository questionRepository, CatalogMapper catalogMapper) {
    this.categoryRepository = categoryRepository;
    this.questionRepository = questionRepository;
    this.catalogMapper = catalogMapper;
  }

  @Override
  public List<CategoryDto> categories() {
    return categoryRepository.findAllByOrderBySortOrderAsc().stream().map(catalogMapper::toCategoryDto).toList();
  }

  @Override
  public List<QuestionDto> questions(String topic, String q) {
    var normalizedTopic = topic == null || topic.isBlank() ? null : topic;
    var search = q == null || q.isBlank() ? null : q;
    var page = PageRequest.of(0, 200);
    var rows = search == null
        ? questionRepository.findByTopic(normalizedTopic, page)
        : questionRepository.findByTopicAndSearch(normalizedTopic, search, page);
    return rows.stream().map(catalogMapper::toQuestionDto).toList();
  }

  @Override
  public QuestionDto question(UUID id) {
    return questionRepository
        .findDetailedById(id)
        .map(catalogMapper::toQuestionDto)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
  }
}
