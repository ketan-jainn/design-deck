package dev.designdeck.api.dto.grading;

import java.util.List;

public record GradeDto(
    int score,
    List<String> missing,
    List<String> wrong,
    List<String> improvements,
    String summary) {}
