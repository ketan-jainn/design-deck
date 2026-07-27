package dev.designdeck.api.dto.grading;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GradeRequest(
    @NotNull UUID questionId,
    @NotBlank @Size(max = 5000) String userAnswer) {}
