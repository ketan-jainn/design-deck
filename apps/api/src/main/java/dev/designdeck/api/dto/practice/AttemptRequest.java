package dev.designdeck.api.dto.practice;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AttemptRequest(
    @NotNull UUID questionId,
    String selfRating,
    String userAnswer,
    Integer aiScore,
    Object aiFeedback) {}
