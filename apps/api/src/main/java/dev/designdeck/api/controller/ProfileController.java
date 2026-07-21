package dev.designdeck.api.controller;

import dev.designdeck.api.dto.profile.ProfileDto;
import dev.designdeck.api.dto.profile.UpdateProfileRequest;
import dev.designdeck.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class ProfileController {
  private final AuthService auth;

  public ProfileController(AuthService auth) {
    this.auth = auth;
  }

  @GetMapping
  public ProfileDto me() {
    return auth.profile(SecurityUtils.currentUserId());
  }

  @PatchMapping
  public ProfileDto updateMe(@Valid @RequestBody UpdateProfileRequest req) {
    return auth.updateProfile(SecurityUtils.currentUserId(), req);
  }
}
