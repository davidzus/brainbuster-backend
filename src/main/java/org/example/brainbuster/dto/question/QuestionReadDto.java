package org.example.brainbuster.dto.question;

import java.util.List;

public record QuestionReadDto(
        Long id,
        String type,
        String difficulty,
        String category,
        String question,
        String correctAnswer,
        List<String> incorrectAnswers
) {}
