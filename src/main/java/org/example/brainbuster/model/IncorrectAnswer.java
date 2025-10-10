package org.example.brainbuster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incorrect_answer")
public class IncorrectAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_text", nullable = false, unique = true, columnDefinition = "TEXT")
    private String answerText;

    @ManyToMany(mappedBy = "incorrectAnswers", fetch = FetchType.LAZY)
    private Set<Question> questions;
}