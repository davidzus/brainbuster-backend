package org.example.brainbuster.repository;

import org.example.brainbuster.model.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @EntityGraph(attributePaths = "incorrectAnswers")
    Optional<Question> findById(Long id);

    @Query("""
        SELECT q FROM Question q
        LEFT JOIN FETCH q.incorrectAnswers ia
        WHERE (:category IS NULL OR LOWER(q.category) = LOWER(:category))
          AND (:difficulty IS NULL OR LOWER(q.difficulty) = LOWER(:difficulty))
          AND (:type IS NULL OR LOWER(q.type) = LOWER(:type))
          AND (:q IS NULL OR LOWER(q.question) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Question> search(
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("type") String type,
            @Param("q") String q,
            Pageable pageable
    );
}