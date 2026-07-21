package dev.designdeck.api.dto.practice;

import java.util.UUID;

public record AttemptRequest(
    UUID questionId,
    String selfRating,
    String userAnswer,
    Integer aiScore,
    Object aiFeedback) {}
