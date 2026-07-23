package dev.designdeck.api.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final String secret;
  private final ObjectMapper mapper;

  public JwtService(@Value("${designdeck.jwt-secret}") String secret, ObjectMapper mapper) {
    this.secret = secret;
    this.mapper = mapper;
  }

  public String issue(UUID userId, Duration ttl) {
    try {
      String header = b64(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      String payload = b64(mapper.writeValueAsBytes(Map.of(
          "sub", userId.toString(),
          "exp", Instant.now().plus(ttl).getEpochSecond())));
      return header + "." + payload + "." + sign(header + "." + payload);
    } catch (Exception e) {
      throw new IllegalStateException("Could not issue token", e);
    }
  }

  public java.util.Optional<UUID> verify(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3 || !MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8))) {
        return java.util.Optional.empty();
      }
      Map<String, Object> payload = mapper.readValue(Base64.getUrlDecoder().decode(parts[1]), new TypeReference<Map<String, Object>>() {});
      if (((Number) payload.get("exp")).longValue() < Instant.now().getEpochSecond()) return java.util.Optional.empty();
      return java.util.Optional.of(UUID.fromString((String) payload.get("sub")));
    } catch (Exception e) {
      return java.util.Optional.empty();
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