package dev.designdeck.api.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.ApiDtos.AnswerKeyDto;
import dev.designdeck.api.dto.ApiDtos.CategoryDto;
import dev.designdeck.api.dto.ApiDtos.QuestionCategoryDto;
import dev.designdeck.api.dto.ApiDtos.QuestionDto;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.repository.CatalogRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogRepositoryImpl implements CatalogRepository {
  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper mapper;

  public CatalogRepositoryImpl(JdbcTemplate jdbcTemplate, ObjectMapper mapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.mapper = mapper;
  }

  @Override
  public List<CategoryDto> categories() {
    return jdbcTemplate.query("select id, name, slug, color, sort_order from categories order by sort_order", (rs, n) ->
        new CategoryDto(rs.getObject("id", UUID.class), rs.getString("name"), rs.getString("slug"), rs.getString("color"), rs.getInt("sort_order")));
  }

  @Override
  public List<QuestionDto> questions(String topic, String q) {
    var sql = """
        select q.id, q.prompt, q.qtype, q.difficulty, q.companies, q.sources,
               c.name category_name, c.slug category_slug, c.color category_color,
               ak.bullets, ak.explanation, ak.follow_ups, ak.common_mistakes, ak.when_not_to_use
        from questions q
        left join categories c on c.id = q.category_id
        left join answer_keys ak on ak.question_id = q.id
        where (? is null or c.slug = ?)
          and (? is null or lower(q.prompt) like lower(?))
        order by q.created_at desc
        limit 200
        """;
    var search = q == null || q.isBlank() ? null : "%" + q + "%";
    return jdbcTemplate.query(sql, ps -> {
      ps.setString(1, topic);
      ps.setString(2, topic);
      ps.setString(3, search);
      ps.setString(4, search);
    }, (rs, n) -> questionDto(rs));
  }

  @Override
  public QuestionDto question(UUID id) {
    try {
      return jdbcTemplate.queryForObject("""
          select q.id, q.prompt, q.qtype, q.difficulty, q.companies, q.sources,
                 c.name category_name, c.slug category_slug, c.color category_color,
                 ak.bullets, ak.explanation, ak.follow_ups, ak.common_mistakes, ak.when_not_to_use
          from questions q
          left join categories c on c.id = q.category_id
          left join answer_keys ak on ak.question_id = q.id
          where q.id = ?
          """, (rs, n) -> questionDto(rs), id);
    } catch (EmptyResultDataAccessException e) {
      throw new ApiException(HttpStatus.NOT_FOUND, "Question not found");
    }
  }

  private QuestionDto questionDto(java.sql.ResultSet rs) throws SQLException {
    var category = rs.getString("category_slug") == null ? null : new QuestionCategoryDto(rs.getString("category_name"), rs.getString("category_slug"), rs.getString("category_color"));
    var answerKey = rs.getString("bullets") == null ? null : new AnswerKeyDto(
        jsonList(rs.getString("bullets")),
        rs.getString("explanation"),
        jsonList(rs.getString("follow_ups")),
        jsonList(rs.getString("common_mistakes")),
        rs.getString("when_not_to_use"));
    return new QuestionDto(
        rs.getObject("id", UUID.class),
        rs.getString("prompt"),
        rs.getString("qtype"),
        rs.getString("difficulty"),
        array(rs.getArray("companies")),
        array(rs.getArray("sources")),
        category,
        answerKey);
  }

  private List<String> jsonList(String json) {
    try {
      return mapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return List.of();
    }
  }

  private List<String> array(java.sql.Array array) throws SQLException {
    return array == null ? List.of() : List.of((String[]) array.getArray());
  }
}