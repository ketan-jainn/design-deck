package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
  @Id
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String color = "#6366f1";

  @Column(name = "sort_order", nullable = false)
  private int sortOrder = 0;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Category() {}

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSlug() {
    return slug;
  }

  public String getColor() {
    return color;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
