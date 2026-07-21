package dev.designdeck.api.dto.catalog;

import java.util.List;
import java.util.UUID;

public record QuestionDto(
    UUID id,
    String prompt,
    String qtype,
    String difficulty,
    List<String> companies,
    List<String> sources,
    QuestionCategoryDto category,
    AnswerKeyDto answerKey) {}
