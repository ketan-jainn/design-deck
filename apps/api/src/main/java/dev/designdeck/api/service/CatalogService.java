package dev.designdeck.api.service;

import dev.designdeck.api.dto.catalog.CategoryDto;
import dev.designdeck.api.dto.catalog.QuestionDto;
import java.util.List;
import java.util.UUID;

public interface CatalogService {
  List<CategoryDto> categories();

  List<QuestionDto> questions(String topic, String q);

  QuestionDto question(UUID id);
}
