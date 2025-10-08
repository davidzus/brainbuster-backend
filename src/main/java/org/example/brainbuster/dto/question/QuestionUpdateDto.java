package org.example.brainbuster.dto.question;

import jakarta.validation.constraints.NotBlank;

public record QuestionUpdateDto(
        @NotBlank String type,
        @NotBlank String difficulty,
        @NotBlank String category,
        @NotBlank String question,
        @NotBlank String correctAnswer,
        java.util.List<@NotBlank String> incorrectAnswers
) {}
