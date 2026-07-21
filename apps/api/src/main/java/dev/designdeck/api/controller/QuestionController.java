package dev.designdeck.api.controller;

import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.service.CatalogService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
  private final CatalogService catalog;

  public QuestionController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  public List<QuestionDto> questions(@RequestParam(required = false) String topic, @RequestParam(required = false) String q) {
    return catalog.questions(topic, q);
  }

  @GetMapping("/{id}")
  public QuestionDto question(@PathVariable UUID id) {
    return catalog.question(id);
  }
}
