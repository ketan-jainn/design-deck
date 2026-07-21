package dev.designdeck.api.dto.catalog;

import java.util.List;

public record AnswerKeyDto(
    List<String> bullets,
    String explanation,
    List<String> followUps,
    List<String> commonMistakes,
    String whenNotToUse) {}
