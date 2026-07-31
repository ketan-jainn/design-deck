package dev.designdeck.api.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

  private static final Logger log = LoggerFactory.getLogger(JwtService.class);
  private static final String BLOCKLIST_PREFIX = "blocklist:";

  private final String secret;
  private final ObjectMapper mapper;
  private final RedisTemplate<String, String> redisTemplate;

  public JwtService(
      @Value("${designdeck.jwt-secret}") String secret,
      ObjectMapper mapper,
      RedisTemplate<String, String> redisTemplate) {
    this.secret = secret;
    this.mapper = mapper;
    this.redisTemplate = redisTemplate;
  }

  public String issue(UUID userId, Duration ttl) {
    try {
      String jti = UUID.randomUUID().toString();
      String header = b64(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      String payload = b64(mapper.writeValueAsBytes(Map.of(
          "sub", userId.toString(),
          "jti", jti,
          "exp", Instant.now().plus(ttl).getEpochSecond())));
      return header + "." + payload + "." + sign(header + "." + payload);
    } catch (Exception e) {
      throw new IllegalStateException("Could not issue token", e);
    }
  }

  public Optional<UUID> verify(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3 ||
          !MessageDigest.isEqual(
              parts[2].getBytes(StandardCharsets.UTF_8),
              sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8))) {
        return Optional.empty();
      }
      Map<String, Object> payload = mapper.readValue(
          Base64.getUrlDecoder().decode(parts[1]),
          new TypeReference<Map<String, Object>>() {});
      long exp = ((Number) payload.get("exp")).longValue();
      if (exp < Instant.now().getEpochSecond()) {
        return Optional.empty();
      }
      String jti = (String) payload.get("jti");
      if (jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKLIST_PREFIX + jti))) {
        return Optional.empty();
      }
      return Optional.of(UUID.fromString((String) payload.get("sub")));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Invalidates a token by storing its jti in Redis with TTL equal to the token's remaining
   * lifetime. Subsequent calls to verify() will reject the token even before expiry.
   */
  public void invalidate(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) return;
      Map<String, Object> payload = mapper.readValue(
          Base64.getUrlDecoder().decode(parts[1]),
          new TypeReference<Map<String, Object>>() {});
      String jti = (String) payload.get("jti");
      long exp = ((Number) payload.get("exp")).longValue();
      if (jti == null) return;
      long remainingSeconds = exp - Instant.now().getEpochSecond();
      if (remainingSeconds > 0) {
        redisTemplate.opsForValue().set(
            BLOCKLIST_PREFIX + jti,
            "1",
            Duration.ofSeconds(remainingSeconds));
        log.debug("Token jti={} invalidated, TTL={}s", jti, remainingSeconds);
      }
    } catch (Exception e) {
      log.warn("Failed to invalidate token: {}", e.getMessage());
    }
  }

  private String sign(String data) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    return b64(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
  }

  private static String b64(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}