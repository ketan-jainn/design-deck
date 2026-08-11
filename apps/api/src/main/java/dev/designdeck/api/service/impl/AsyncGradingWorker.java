package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.catalog.AnswerKeyDto;
import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.entity.GradingJob;
import dev.designdeck.api.repository.GradingJobRepository;
import dev.designdeck.api.service.CatalogService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AsyncGradingWorker {
  private static final Logger log = LoggerFactory.getLogger(AsyncGradingWorker.class);
  private final CatalogService catalogService;
  private final ObjectMapper mapper;
  private final MeterRegistry meterRegistry;
  private final GradingJobRepository gradingJobRepository;
  private final SseEmitterRegistry sseEmitterRegistry;
  private final String apiKey;
  private final String model;
  private final HttpClient client = HttpClient.newHttpClient();

  public AsyncGradingWorker(
      CatalogService catalogService,
      ObjectMapper mapper,
      MeterRegistry meterRegistry,
      GradingJobRepository gradingJobRepository,
      SseEmitterRegistry sseEmitterRegistry,
      @Value("${designdeck.gemini-api-key:}") String apiKey,
      @Value("${designdeck.gemini-model:}") String model) {
    this.catalogService = catalogService;
    this.mapper = mapper;
    this.meterRegistry = meterRegistry;
    this.gradingJobRepository = gradingJobRepository;
    this.sseEmitterRegistry = sseEmitterRegistry;
    this.apiKey = apiKey;
    this.model = model;
  }

  @Async("gradingExecutor")
  public void processJob(UUID jobId, UUID questionId, String userAnswer) {
    GradingJob job = gradingJobRepository.findById(jobId).orElse(null);
    if (job == null) return;
    
    job.setStatus(GradingJob.Status.PROCESSING);
    gradingJobRepository.save(job);

    try {
      if (apiKey == null || apiKey.isBlank()) {
        throw new RuntimeException("Missing GEMINI_API_KEY");
      }
      QuestionDto q = catalogService.question(questionId);
      AnswerKeyDto answerKey = q.answerKey();
      if (answerKey == null) {
        throw new RuntimeException("Question not found");
      }
      
      String system = "Grade an SDE-2 system-design answer. Return only JSON: {\"score\":0,\"missing\":[],\"wrong\":[],\"improvements\":[],\"summary\":\"\"}. Keep feedback concise.";
      String user = "QUESTION:\n" + q.prompt() + "\n\nANSWER KEY:\n" + answerKey.bullets() +
          "\n\nEXPLANATION:\n" + answerKey.explanation() + "\n\nCOMMON MISTAKES:\n" + answerKey.commonMistakes() +
          "\n\nCANDIDATE ANSWER:\n" + userAnswer;

      String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model +
          ":generateContent?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
      
      String body = mapper.writeValueAsString(Map.of(
          "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
          "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", user)))),
          "generationConfig", Map.of("responseMimeType", "application/json")));
          
      Timer.Sample sample = Timer.start(meterRegistry);
      HttpResponse<String> response = client.sendAsync(
          HttpRequest.newBuilder(URI.create(url))
              .timeout(Duration.ofSeconds(30))
              .header("content-type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body))
              .build(),
          HttpResponse.BodyHandlers.ofString()).join();
      sample.stop(meterRegistry.timer("grading.duration"));
      
      if (response.statusCode() >= 400) {
        throw new RuntimeException("AI grading failed: HTTP " + response.statusCode());
      }
      
      String text = mapper.readTree(response.body())
          .path("candidates").path(0)
          .path("content").path("parts").path(0)
          .path("text").asText("{}");
          
      JsonNode node = mapper.readTree(text);
      GradeDto gradeDto = new GradeDto(
          clamp(node.path("score").asInt()),
          strings(node.path("missing")),
          strings(node.path("wrong")),
          strings(node.path("improvements")),
          node.path("summary").asText(""));
          
      job.setResult(mapper.writeValueAsString(gradeDto));
      job.setStatus(GradingJob.Status.DONE);
      gradingJobRepository.save(job);
      meterRegistry.counter("grading.jobs", "status", "done").increment();
      
      sseEmitterRegistry.emitComplete(jobId, gradeDto);
      
    } catch (Exception e) {
      log.error("Job {} failed: {}", jobId, e.getMessage());
      job.setErrorMessage(e.getMessage());
      job.setStatus(GradingJob.Status.FAILED);
      gradingJobRepository.save(job);
      meterRegistry.counter("grading.jobs", "status", "failed").increment();
      sseEmitterRegistry.emitError(jobId, e.getMessage());
    }
  }

  private int clamp(int value) { return Math.max(0, Math.min(100, value)); }

  private List<String> strings(JsonNode node) {
    ArrayList<String> out = new ArrayList<>();
    if (node.isArray()) {
      node.forEach(v -> out.add(v.asText()));
    }
    return out;
  }
}
