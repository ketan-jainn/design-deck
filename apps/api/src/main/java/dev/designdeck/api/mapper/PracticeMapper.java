package dev.designdeck.api.mapper;

import dev.designdeck.api.dto.practice.TopicMasteryDto;
import dev.designdeck.api.repository.UserCardStateRepository.TopicMasteryRow;
import org.springframework.stereotype.Component;

@Component
public class PracticeMapper {
  public TopicMasteryDto toTopicMasteryDto(TopicMasteryRow row) {
    int seen = row.getSeen();
    int correct = row.getCorrect();
    int mastery = (int) Math.round((float) correct * 100 / Math.max(1, seen));
    return new TopicMasteryDto(row.getName(), row.getSlug(), row.getColor(), mastery);
  }
}
