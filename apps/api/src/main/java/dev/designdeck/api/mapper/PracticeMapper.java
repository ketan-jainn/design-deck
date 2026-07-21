package dev.designdeck.api.mapper;

import dev.designdeck.api.dto.practice.TopicMasteryDto;
import dev.designdeck.api.repository.UserCardStateRepository.TopicMasteryRow;
import org.springframework.stereotype.Component;

@Component
public class PracticeMapper {
  public TopicMasteryDto toTopicMasteryDto(TopicMasteryRow row) {
    var seen = row.getSeen();
    var correct = row.getCorrect();
    var mastery = Math.round((float) correct * 100 / Math.max(1, seen));
    return new TopicMasteryDto(row.getName(), row.getSlug(), row.getColor(), mastery);
  }
}
