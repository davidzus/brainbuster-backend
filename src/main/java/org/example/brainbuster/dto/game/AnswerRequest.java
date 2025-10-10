package org.example.brainbuster.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AnswerRequest(
        @NotBlank
        @Schema(example = "7b8e8c2f-3d1b-4a92-8c0a-0f1d2e3c4b5a")
        String choiceId
) {}
