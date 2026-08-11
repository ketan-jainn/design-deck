package dev.designdeck.api.service.impl;

import dev.designdeck.api.dto.grading.GradeDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
  private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

  public void register(UUID jobId, SseEmitter emitter) {
    emitters.put(jobId, emitter);
    emitter.onCompletion(() -> emitters.remove(jobId));
    emitter.onTimeout(() -> emitters.remove(jobId));
    emitter.onError(e -> emitters.remove(jobId));
  }

  public void emitComplete(UUID jobId, GradeDto result) {
    SseEmitter emitter = emitters.remove(jobId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("complete").data(result));
        emitter.complete();
      } catch (Exception e) {
        // Ignored
      }
    }
  }

  public void emitError(UUID jobId, String errorMessage) {
    SseEmitter emitter = emitters.remove(jobId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("error").data(Map.of("message", errorMessage)));
        emitter.completeWithError(new RuntimeException(errorMessage));
      } catch (Exception e) {
        // Ignored
      }
    }
  }
}
