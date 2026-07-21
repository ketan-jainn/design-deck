package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.service.CatalogService;
import dev.designdeck.api.service.GradingService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GradingServiceImpl implements GradingService {
  private final CatalogService catalogService;
  private final ObjectMapper mapper;
  private final String apiKey;
  private final String model;
  private final HttpClient client = HttpClient.newHttpClient();

  public GradingServiceImpl(
      CatalogService catalogService,
      ObjectMapper mapper,
      @Value("${designdeck.openai-api-key}") String apiKey,
      @Value("${designdeck.openai-model}") String model) {
    this.catalogService = catalogService;
    this.mapper = mapper;
    this.apiKey = apiKey;
    this.model = model;
  }

  @Override
  public GradeDto grade(GradeRequest req) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Missing OPENAI_API_KEY");
    }
    var q = catalogService.question(req.questionId());
    var answerKey = q.answerKey();
    if (answerKey == null) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Question not found");
    }
    var system =
        "Grade an SDE-2 system-design answer. Return only JSON: {\"score\":0,\"missing\":[],\"wrong\":[],\"improvements\":[],\"summary\":\"\"}. Keep feedback concise.";
    var user = "QUESTION:\n"
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
      var body = mapper.writeValueAsString(Map.of(
          "model",
          model,
          "response_format",
          Map.of("type", "json_object"),
          "messages",
          List.of(Map.of("role", "system", "content", system), Map.of("role", "user", "content", user))));
      var response = client
          .sendAsync(
              HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                  .timeout(Duration.ofSeconds(30))
                  .header("content-type", "application/json")
                  .header("authorization", "Bearer " + apiKey)
                  .POST(HttpRequest.BodyPublishers.ofString(body))
                  .build(),
              HttpResponse.BodyHandlers.ofString())
          .join();
      if (response.statusCode() >= 400) {
        throw new ApiException(HttpStatus.BAD_GATEWAY, "AI grading failed");
      }
      var text = mapper.readTree(response.body()).path("choices").path(0).path("message").path("content").asText("{}");
      var node = mapper.readTree(text);
      return new GradeDto(
          clamp(node.path("score").asInt()),
          strings(node.path("missing")),
          strings(node.path("wrong")),
          strings(node.path("improvements")),
          node.path("summary").asText(""));
    } catch (ApiException e) {
      throw e;
    } catch (java.io.IOException e) {
      throw new ApiException(HttpStatus.BAD_GATEWAY, "AI grading failed");
    }
  }

  private int clamp(int value) {
    return Math.max(0, Math.min(100, value));
  }

  private List<String> strings(com.fasterxml.jackson.databind.JsonNode node) {
    var out = new ArrayList<String>();
    if (node.isArray()) {
      node.forEach(v -> out.add(v.asText()));
    }
    return out;
  }
}
