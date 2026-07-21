package dev.designdeck.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.designdeck.api.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
  @Query("""
      select q from Question q
      left join fetch q.category c
      left join fetch q.answerKey ak
      where q.id = :id
      """)
  Optional<Question> findDetailedById(@Param("id") UUID id);

  @Query(
      value = """
          select q.id from questions q
          where not exists (
            select 1 from user_card_state s
            where s.user_id = :userId and s.question_id = q.id
          )
          order by random()
          limit :limit
          """,
      nativeQuery = true)
  List<UUID> findRandomUnseenIds(@Param("userId") UUID userId, @Param("limit") int limit);
  @Query("""
    select q from Question q
    left join fetch q.category c
    left join fetch q.answerKey ak
    where (:topic is null or c.slug = :topic)
    order by q.createdAt desc
    """)
  List<Question> findByTopic(@Param("topic") String topic, Pageable pageable);

  @Query("""
      select q from Question q
      left join fetch q.category c
      left join fetch q.answerKey ak
      where (:topic is null or c.slug = :topic)
        and lower(q.prompt) like lower(concat('%', :search, '%'))
      order by q.createdAt desc
      """)
  List<Question> findByTopicAndSearch(
      @Param("topic") String topic, @Param("search") String search, Pageable pageable);
}
