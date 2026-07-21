package dev.designdeck.api.controller;

import dev.designdeck.api.dto.catalog.CategoryDto;
import dev.designdeck.api.service.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final CatalogService catalog;

  public CategoryController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping
  public List<CategoryDto> categories() {
    return catalog.categories();
  }
}
