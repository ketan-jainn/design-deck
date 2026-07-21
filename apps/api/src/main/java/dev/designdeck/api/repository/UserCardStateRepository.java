package dev.designdeck.api.repository;

import dev.designdeck.api.entity.UserCardState;
import dev.designdeck.api.entity.UserCardStateId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCardStateRepository extends JpaRepository<UserCardState, UserCardStateId> {
  @Query("""
      select s from UserCardState s
      where s.user.id = :userId and s.dueAt <= CURRENT_TIMESTAMP
      order by s.dueAt
      """)
  List<UserCardState> findDueCards(@Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);

  Optional<UserCardState> findByUser_IdAndQuestion_Id(UUID userId, UUID questionId);

  @Query("""
      select coalesce(sum(s.timesSeen), 0), coalesce(sum(s.timesCorrect), 0)
      from UserCardState s where s.user.id = :userId
      """)
  Object[] sumTotals(@Param("userId") UUID userId);

  @Query("""
      select count(s) from UserCardState s
      where s.user.id = :userId and s.dueAt <= CURRENT_TIMESTAMP
      """)
  long countDue(@Param("userId") UUID userId);

  @Query(
      value = """
          select c.name as name, c.slug as slug, c.color as color,
                 coalesce(sum(s.times_seen), 0) as seen,
                 coalesce(sum(s.times_correct), 0) as correct
          from user_card_state s
          join questions q on q.id = s.question_id
          join categories c on c.id = q.category_id
          where s.user_id = :userId
          group by c.name, c.slug, c.color
          """,
      nativeQuery = true)
  List<TopicMasteryRow> topicMastery(@Param("userId") UUID userId);

  interface TopicMasteryRow {
    String getName();

    String getSlug();

    String getColor();

    int getSeen();

    int getCorrect();
  }
}
