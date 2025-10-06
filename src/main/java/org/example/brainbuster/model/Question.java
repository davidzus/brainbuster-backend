package org.example.brainbuster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, columnDefinition = "TEXT")
    private String type;

    @Column(name = "difficulty", nullable = false, columnDefinition = "TEXT")
    private String difficulty;

    @Column(name = "category", nullable = false, columnDefinition = "TEXT")
    private String category;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @OneToMany(
            mappedBy = "question",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<IncorrectAnswer> incorrectAnswers = new HashSet<>();

    public void addIncorrectAnswer(IncorrectAnswer ia) {
        incorrectAnswers.add(ia);
        ia.setQuestion(this);
    }
    public void removeIncorrectAnswer(IncorrectAnswer ia) {
        incorrectAnswers.remove(ia);
        ia.setQuestion(null);
    }
}