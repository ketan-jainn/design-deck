package dev.designdeck.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtService = new JwtService("my-super-secret-key-that-is-at-least-32-bytes-long", objectMapper, redisTemplate);
    }

    @Test
    void issue_thenVerify_returnsCorrectUserId() {
        UUID userId = UUID.randomUUID();
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        String token = jwtService.issue(userId, Duration.ofMinutes(10));
        Optional<UUID> verifiedUserId = jwtService.verify(token);

        assertThat(verifiedUserId).isPresent().contains(userId);
    }

    @Test
    void expired_token_returnsEmpty() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        String token = jwtService.issue(userId, Duration.ofSeconds(0));
        
        Thread.sleep(1000);
        
        Optional<UUID> verifiedUserId = jwtService.verify(token);
        assertThat(verifiedUserId).isEmpty();
    }

    @Test
    void tampered_signature_returnsEmpty() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.issue(userId, Duration.ofMinutes(10));
        
        String tamperedToken = token.substring(0, token.length() - 5) + "abcde";
        
        Optional<UUID> verifiedUserId = jwtService.verify(tamperedToken);
        assertThat(verifiedUserId).isEmpty();
    }

    @Test
    void invalidate_thenVerify_returnsEmpty() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.issue(userId, Duration.ofMinutes(10));
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtService.invalidate(token);
        
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        Optional<UUID> verifiedUserId = jwtService.verify(token);
        
        assertThat(verifiedUserId).isEmpty();
    }
}
