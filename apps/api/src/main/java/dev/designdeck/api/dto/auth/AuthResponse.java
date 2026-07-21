package dev.designdeck.api.dto.auth;

import dev.designdeck.api.dto.profile.ProfileDto;

public record AuthResponse(String accessToken, String refreshToken, ProfileDto profile) {}
