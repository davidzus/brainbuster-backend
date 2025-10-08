package org.example.brainbuster.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionCreateDto(
        @NotBlank String type,
        @NotBlank String difficulty,
        @NotBlank String category,
        @NotBlank String question,
        @NotBlank String correctAnswer,
        @Size(min = 1) List<@NotBlank String> incorrectAnswers
) {}
