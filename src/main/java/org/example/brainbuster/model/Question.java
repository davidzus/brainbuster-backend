package org.example.brainbuster.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "questions")
@ToString(exclude = "incorrectAnswers")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "difficulty", nullable = false, length = 50)
    private String difficulty;

    @Column(name = "category", nullable = false, length = 100)
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
    @jakarta.persistence.OrderColumn(name = "position") // optional
    private java.util.List<IncorrectAnswer> incorrectAnswers = new java.util.ArrayList<>();

    public void addIncorrectAnswer(IncorrectAnswer ia) {
        incorrectAnswers.add(ia);
        ia.setQuestion(this);
    }
    public void removeIncorrectAnswer(IncorrectAnswer ia) {
        incorrectAnswers.remove(ia);
        ia.setQuestion(null);
    }
}
