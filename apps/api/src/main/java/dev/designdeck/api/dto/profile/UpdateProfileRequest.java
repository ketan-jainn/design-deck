package dev.designdeck.api.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank @Size(min = 1, max = 60) String displayName,
    @Min(1) @Max(100) int dailyGoal) {}
