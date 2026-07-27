package dev.designdeck.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 8, max = 128) String password) {}
