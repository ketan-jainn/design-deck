package dev.designdeck.api.controller;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
  private SecurityUtils() {}

  public static UUID currentUserId() {
    return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
