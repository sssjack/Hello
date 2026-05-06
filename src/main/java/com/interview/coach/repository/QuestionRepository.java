package com.interview.coach.repository;

import com.interview.coach.domain.ExamType;
import com.interview.coach.domain.Question;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestionRepository {

    private final JdbcClient jdbcClient;

    public QuestionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Question> findById(Long id) {
        return jdbcClient.sql("""
                        select id, exam_year, exam_type, province, source, tags, content, created_at
                        from questions where id = :id
                        """)
                .param("id", id)
                .query(this::mapQuestion)
                .optional();
    }

    public List<Question> findByFilters(Integer year, ExamType type) {
        StringBuilder sql = new StringBuilder("""
                select id, exam_year, exam_type, province, source, tags, content, created_at
                from questions where enabled = true
                """);
        List<Object> args = new ArrayList<>();
        if (year != null) {
            sql.append(" and exam_year = ?");
            args.add(year);
        }
        if (type != null) {
            sql.append(" and exam_type = ?");
            args.add(type.name());
        }
        sql.append(" order by rand() limit 50");
        return jdbcClient.sql(sql.toString())
                .params(args)
                .query(this::mapQuestion)
                .list();
    }

    public List<Integer> findYears() {
        return jdbcClient.sql("select distinct exam_year from questions where enabled = true order by exam_year desc")
                .query(Integer.class)
                .list();
    }

    private Question mapQuestion(ResultSet rs, int rowNum) throws SQLException {
        return new Question(
                rs.getLong("id"),
                rs.getInt("exam_year"),
                ExamType.valueOf(rs.getString("exam_type")),
                rs.getString("province"),
                rs.getString("source"),
                rs.getString("tags"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
