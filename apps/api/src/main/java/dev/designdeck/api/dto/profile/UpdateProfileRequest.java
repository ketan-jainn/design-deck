package dev.designdeck.api.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(@NotBlank String displayName, @Min(1) @Max(100) int dailyGoal) {}
