package dev.designdeck.api.dto.practice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SessionRequest(@Min(5) @Max(50) int size) {}
