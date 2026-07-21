package dev.designdeck.api.service.impl;

import dev.designdeck.api.dto.auth.AuthResponse;
import dev.designdeck.api.dto.auth.LoginRequest;
import dev.designdeck.api.dto.auth.SignupRequest;
import dev.designdeck.api.dto.profile.ProfileDto;
import dev.designdeck.api.dto.profile.UpdateProfileRequest;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.PasswordResetToken;
import dev.designdeck.api.entity.Profile;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.mapper.AuthMapper;
import dev.designdeck.api.repository.AppUserRepository;
import dev.designdeck.api.repository.PasswordResetTokenRepository;
import dev.designdeck.api.repository.ProfileRepository;
import dev.designdeck.api.service.AuthService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
  private final AppUserRepository appUserRepository;
  private final ProfileRepository profileRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final AuthMapper authMapper;
  private final PasswordEncoder encoder;
  private final String frontendUrl;

  public AuthServiceImpl(
      AppUserRepository appUserRepository,
      ProfileRepository profileRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      AuthMapper authMapper,
      PasswordEncoder encoder,
      @Value("${designdeck.frontend-url}") String frontendUrl) {
    this.appUserRepository = appUserRepository;
    this.profileRepository = profileRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.authMapper = authMapper;
    this.encoder = encoder;
    this.frontendUrl = frontendUrl;
  }

  @Override
  public AuthResponse signup(
      SignupRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer) {
    var email = req.email().toLowerCase();
    var user = new AppUser(email, encoder.encode(req.password()));
    var displayName = req.displayName() == null || req.displayName().isBlank() ? email.split("@")[0] : req.displayName();
    user.setProfile(new Profile(user, displayName));
    appUserRepository.save(user);
    var userId = user.getId();
    return new AuthResponse(
        accessTokenIssuer.apply(userId), refreshTokenIssuer.apply(userId), profile(userId));
  }

  @Override
  public AuthResponse login(
      LoginRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer) {
    var user = appUserRepository
        .findByEmail(req.email().toLowerCase())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    if (!encoder.matches(req.password(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    return new AuthResponse(
        accessTokenIssuer.apply(user.getId()),
        refreshTokenIssuer.apply(user.getId()),
        profile(user.getId()));
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileDto profile(UUID userId) {
    var profile = requireProfile(userId);
    return authMapper.toProfileDto(profile.getUser(), profile);
  }

  @Override
  public ProfileDto updateProfile(UUID userId, UpdateProfileRequest req) {
    var profile = requireProfile(userId);
    profile.setDisplayName(req.displayName());
    profile.setDailyGoal(req.dailyGoal());
    profileRepository.save(profile);
    return authMapper.toProfileDto(profile.getUser(), profile);
  }

  @Override
  public void createResetToken(String email) {
    appUserRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
      var token = UUID.randomUUID().toString() + UUID.randomUUID();
      passwordResetTokenRepository.save(new PasswordResetToken(token, user, Instant.now().plus(Duration.ofHours(1))));
      System.out.println("Password reset link: " + frontendUrl + "/reset-password?token=" + token);
    });
  }

  @Override
  public void resetPassword(String token, String password) {
    var resetToken = passwordResetTokenRepository
        .findValidToken(token)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired"));
    var user = resetToken.getUser();
    user.setPasswordHash(encoder.encode(password));
    appUserRepository.save(user);
    resetToken.markUsed();
    passwordResetTokenRepository.save(resetToken);
  }

  private Profile requireProfile(UUID userId) {
    return profileRepository
        .findDetailedById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
  }
}
