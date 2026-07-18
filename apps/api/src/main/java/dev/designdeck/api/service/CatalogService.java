package dev.designdeck.api.service;

import dev.designdeck.api.dto.ApiDtos.CategoryDto;
import dev.designdeck.api.dto.ApiDtos.QuestionDto;
import java.util.List;
import java.util.UUID;
import dev.designdeck.api.repository.CatalogRepository;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {
  private final CatalogRepository repository;

  public CatalogService(CatalogRepository repository) {
    this.repository = repository;
  }

  public List<CategoryDto> categories() {
    return repository.categories();
  }

  public List<QuestionDto> questions(String topic, String q) {
    return repository.questions(topic, q);
  }

  public QuestionDto question(UUID id) {
    return repository.question(id);
  }
}