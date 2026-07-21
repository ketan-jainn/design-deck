package dev.designdeck.api.dto.catalog;

import java.util.UUID;

public record CategoryDto(UUID id, String name, String slug, String color, int sortOrder) {}
