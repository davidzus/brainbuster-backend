package org.example.brainbuster.repository;

import org.example.brainbuster.model.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @EntityGraph(attributePaths = "incorrectAnswers")
    Optional<Question> findById(Long id);
}