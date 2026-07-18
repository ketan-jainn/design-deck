package dev.designdeck.api.repository;

import dev.designdeck.api.dto.ApiDtos.CategoryDto;
import dev.designdeck.api.dto.ApiDtos.QuestionDto;
import java.util.List;
import java.util.UUID;

public interface CatalogRepository {
  List<CategoryDto> categories();

  List<QuestionDto> questions(String topic, String q);

  QuestionDto question(UUID id);
}