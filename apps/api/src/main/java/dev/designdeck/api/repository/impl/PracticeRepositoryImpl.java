package dev.designdeck.api.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.ApiDtos.AttemptRequest;
import dev.designdeck.api.dto.ApiDtos.TopicMasteryDto;
import dev.designdeck.api.repository.PracticeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PracticeRepositoryImpl implements PracticeRepository {
  private final JdbcTemplate jdbc;

  public PracticeRepositoryImpl(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
  }

  @Override
  public List<UUID> dueQuestionIds(UUID userId, int limit) {
    return jdbc.query("select question_id from user_card_state where user_id = ? and due_at <= now() order by due_at limit ?", (rs, n) -> rs.getObject("question_id", UUID.class), userId, limit);
  }

  @Override
  public List<UUID> randomQuestionIds(UUID userId, int limit) {
    return jdbc.query("""
        select q.id from questions q
        where not exists (select 1 from user_card_state s where s.user_id = ? and s.question_id = q.id)
        order by random() limit ?
        """, (rs, n) -> rs.getObject("id", UUID.class), userId, limit);
  }

  @Override
  public void saveAttempt(UUID userId, AttemptRequest req, String aiFeedbackJson) {
    jdbc.update("insert into attempts (user_id, question_id, self_rating, ai_score, user_answer, ai_feedback) values (?, ?, ?, ?, ?, ?::jsonb)",
        userId, req.questionId(), req.selfRating(), req.aiScore(), req.userAnswer(), aiFeedbackJson);
  }

  @Override
  public CardState cardState(UUID userId, UUID questionId) {
    return jdbc.query("select ease, interval_days, times_seen, times_correct from user_card_state where user_id = ? and question_id = ?",
        rs -> rs.next() ? new CardState(rs.getDouble("ease"), rs.getInt("interval_days"), rs.getInt("times_seen"), rs.getInt("times_correct")) : null,
        userId, questionId);
  }

  @Override
  public void upsertCardState(UUID userId, UUID questionId, double ease, int intervalDays, Instant dueAt, int timesSeen, int timesCorrect, String lastResult) {
    jdbc.update("""
        insert into user_card_state (user_id, question_id, ease, interval_days, due_at, times_seen, times_correct, last_result, updated_at)
        values (?, ?, ?, ?, ?, ?, ?, ?, now())
        on conflict (user_id, question_id) do update set
          ease = excluded.ease, interval_days = excluded.interval_days, due_at = excluded.due_at,
          times_seen = excluded.times_seen, times_correct = excluded.times_correct,
          last_result = excluded.last_result, updated_at = now()
        """, userId, questionId, ease, intervalDays, dueAt, timesSeen, timesCorrect, lastResult);
  }

  @Override
  public ProfileStats profileStats(UUID userId) {
    var row = jdbc.queryForMap("select streak_count, daily_goal, last_active_date from profiles where user_id = ?", userId);
    return new ProfileStats(((Number) row.get("streak_count")).intValue(), ((Number) row.get("daily_goal")).intValue(), (LocalDate) row.get("last_active_date"));
  }

  @Override
  public Totals totals(UUID userId) {
    var row = jdbc.queryForMap("select coalesce(sum(times_seen),0) seen, coalesce(sum(times_correct),0) correct from user_card_state where user_id = ?", userId);
    return new Totals(((Number) row.get("seen")).intValue(), ((Number) row.get("correct")).intValue());
  }

  @Override
  public Integer todayAttemptCount(UUID userId) {
    return jdbc.queryForObject("select count(*) from attempts where user_id = ? and created_at >= date_trunc('day', now())", Integer.class, userId);
  }

  @Override
  public Integer dueCount(UUID userId) {
    return jdbc.queryForObject("select count(*) from user_card_state where user_id = ? and due_at <= now()", Integer.class, userId);
  }

  @Override
  public List<TopicMasteryDto> topicMastery(UUID userId) {
    return jdbc.query("""
        select c.name, c.slug, c.color, sum(s.times_seen) seen, sum(s.times_correct) correct
        from user_card_state s join questions q on q.id = s.question_id join categories c on c.id = q.category_id
        where s.user_id = ?
        group by c.name, c.slug, c.color
        """, (rs, n) -> new TopicMasteryDto(rs.getString("name"), rs.getString("slug"), rs.getString("color"), Math.round((float) rs.getInt("correct") * 100 / Math.max(1, rs.getInt("seen")))), userId);
  }

  @Override
  public void updateStreak(UUID userId, int streakCount, LocalDate lastActiveDate) {
    jdbc.update("update profiles set streak_count = ?, last_active_date = ?, updated_at = now() where user_id = ?", streakCount, lastActiveDate, userId);
  }
}