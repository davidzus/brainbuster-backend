package org.example.brainbuster.repository;


import org.example.brainbuster.model.IncorrectAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncorrectAnswerRepository extends JpaRepository<IncorrectAnswer, Long> {
}
