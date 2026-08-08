package dev.designdeck.api.service.impl;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.designdeck.api.dto.catalog.AnswerKeyDto;
import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.service.CatalogService;
import dev.designdeck.api.service.GradingService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Service
public class GradingServiceImpl implements GradingService {
  private final CatalogService catalogService;
  private final ObjectMapper mapper;
  private final String apiKey;
  private final String model;
  private final MeterRegistry meterRegistry;
  private final HttpClient client = HttpClient.newHttpClient();

  public GradingServiceImpl(
      CatalogService catalogService,
      ObjectMapper mapper,
      MeterRegistry meterRegistry,
      @Value("${designdeck.gemini-api-key}") String apiKey,
      @Value("${designdeck.gemini-model}") String model) {
    this.catalogService = catalogService;
    this.mapper = mapper;
    this.meterRegistry = meterRegistry;
    this.apiKey = apiKey;
    this.model = model;
  }

  @Override
  public GradeDto grade(GradeRequest req) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Missing GEMINI_API_KEY");
    }
    QuestionDto q = catalogService.question(req.questionId());
    AnswerKeyDto answerKey = q.answerKey();
    if (answerKey == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Question not found");
    }
    String system =
        "Grade an SDE-2 system-design answer. Return only JSON: {\"score\":0,\"missing\":[],\"wrong\":[],\"improvements\":[],\"summary\":\"\"}. Keep feedback concise.";
    String user = "QUESTION:\n"
        + q.prompt()
        + "\n\nANSWER KEY:\n"
        + answerKey.bullets()
        + "\n\nEXPLANATION:\n"
        + answerKey.explanation()
        + "\n\nCOMMON MISTAKES:\n"
        + answerKey.commonMistakes()
        + "\n\nCANDIDATE ANSWER:\n"
        + req.userAnswer();
    try {
      String url = "https://generativelanguage.googleapis.com/v1beta/models/"
          + model
          + ":generateContent?key="
          + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
      String body = mapper.writeValueAsString(Map.of(
          "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
          "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", user)))),
          "generationConfig", Map.of("responseMimeType", "application/json")));
      Timer.Sample sample = Timer.start(meterRegistry);
      HttpResponse<String> response = client
          .sendAsync(
              HttpRequest.newBuilder(URI.create(url))
                  .timeout(Duration.ofSeconds(30))
                  .header("content-type", "application/json")
                  .POST(HttpRequest.BodyPublishers.ofString(body))
                  .build(),
              HttpResponse.BodyHandlers.ofString())
          .join();
      sample.stop(meterRegistry.timer("grading.duration"));
      if (response.statusCode() >= 400) {
        throw new ApiException(HttpStatus.BAD_GATEWAY, "AI grading failed");
      }
      String text = mapper
          .readTree(response.body())
          .path("candidates")
          .path(0)
          .path("content")
          .path("parts")
          .path(0)
          .path("text")
          .asText("{}");
      JsonNode node = mapper.readTree(text);
      return new GradeDto(
          clamp(node.path("score").asInt()),
          strings(node.path("missing")),
          strings(node.path("wrong")),
          strings(node.path("improvements")),
          node.path("summary").asText(""));
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "AI grading failed: " + e.getMessage());
    }
  }

  private String geminiError(String body) {
    try {
      return mapper.readTree(body).path("error").path("message").asText(body);
    } catch (Exception e) {
      return body;
    }
  }

  private int clamp(int value) {
    return Math.max(0, Math.min(100, value));
  }

  private List<String> strings(JsonNode node) {
    ArrayList<String> out = new ArrayList<>();
    if (node.isArray()) {
      node.forEach(v -> out.add(v.asText()));
    }
    return out;
  }
}
