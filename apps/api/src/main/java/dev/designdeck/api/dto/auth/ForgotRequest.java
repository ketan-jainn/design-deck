package dev.designdeck.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotRequest(@NotBlank @Email String email) {}
