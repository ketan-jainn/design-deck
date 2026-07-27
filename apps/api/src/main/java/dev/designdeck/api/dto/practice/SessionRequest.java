package dev.designdeck.api.dto.practice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SessionRequest(@Min(1) @Max(20) int size) {}
