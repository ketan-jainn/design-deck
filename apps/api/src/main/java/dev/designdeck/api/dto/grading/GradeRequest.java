package dev.designdeck.api.dto.grading;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record GradeRequest(UUID questionId, @NotBlank @Size(max = 4000) String userAnswer) {}
