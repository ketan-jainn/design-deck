package dev.designdeck.api.service.impl;

import dev.designdeck.api.service.RateLimiterService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Sliding window counter rate limiter using Redis.
 *
 * Strategy: one Redis key per (userId, action, window-bucket).
 * INCR + EXPIRE pattern: first increment sets the key and TTL; subsequent increments
 * within the window just bump the counter. When the counter exceeds the limit, deny.
 *
 * Trade-off: this is a fixed-window approximation (not a true sliding log) but is O(1)
 * in both time and space — suitable for per-user API rate limiting.
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService {

  private final RedisTemplate<String, String> redisTemplate;

  public RateLimiterServiceImpl(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public boolean tryConsume(UUID userId, String action, int limit, Duration window) {
    String key = "ratelimit:" + action + ":" + userId.toString();
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == null) return false;
    if (count == 1) {
      // First request in this window — set the expiry
      redisTemplate.expire(key, window);
    }
    return count <= limit;
  }
}
