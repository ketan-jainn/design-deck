package dev.designdeck.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetRequest(@NotBlank String token, @Size(min = 6, max = 200) String password) {}
