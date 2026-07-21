package dev.designdeck.api.mapper;

import dev.designdeck.api.dto.catalog.AnswerKeyDto;
import dev.designdeck.api.dto.catalog.CategoryDto;
import dev.designdeck.api.dto.catalog.QuestionCategoryDto;
import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.entity.AnswerKey;
import dev.designdeck.api.entity.Category;
import dev.designdeck.api.entity.Question;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {
  public CategoryDto toCategoryDto(Category category) {
    return new CategoryDto(category.getId(), category.getName(), category.getSlug(), category.getColor(), category.getSortOrder());
  }

  public QuestionDto toQuestionDto(Question question) {
    var category = question.getCategory();
    var questionCategory = category == null
        ? null
        : new QuestionCategoryDto(category.getName(), category.getSlug(), category.getColor());
    var answerKey = question.getAnswerKey();
    return new QuestionDto(
        question.getId(),
        question.getPrompt(),
        question.getQtype().name(),
        question.getDifficulty().name(),
        toList(question.getCompanies()),
        toList(question.getSources()),
        questionCategory,
        answerKey == null ? null : toAnswerKeyDto(answerKey));
  }

  private AnswerKeyDto toAnswerKeyDto(AnswerKey answerKey) {
    return new AnswerKeyDto(
        answerKey.getBullets(),
        answerKey.getExplanation(),
        answerKey.getFollowUps(),
        answerKey.getCommonMistakes(),
        answerKey.getWhenNotToUse());
  }

  private List<String> toList(String[] values) {
    return values == null ? List.of() : Arrays.asList(values);
  }
}
