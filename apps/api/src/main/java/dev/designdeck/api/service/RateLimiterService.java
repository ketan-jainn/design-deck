package dev.designdeck.api.service;

import java.time.Duration;
import java.util.UUID;

/**
 * Sliding window rate limiter backed by Redis.
 * Each call to tryConsume increments a per-user counter; returns false when the limit is exceeded.
 */
public interface RateLimiterService {

  /**
   * Attempts to consume one token for the given user and action.
   *
   * @param userId the authenticated user
   * @param action an identifier for the rate-limited action (e.g. "grade")
   * @param limit  maximum number of calls allowed within the window
   * @param window the rolling time window
   * @return true if the request is allowed, false if the rate limit is exceeded
   */
  boolean tryConsume(UUID userId, String action, int limit, Duration window);
}
