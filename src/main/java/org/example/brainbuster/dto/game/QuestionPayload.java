package org.example.brainbuster.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Question shown to the player (never leaks the correct answer)")
public record QuestionPayload(
        long questionId,
        String prompt,
        List<Choice> choices,
        int index,   // 0-based
        int total
) {
    @Schema(description = "Opaque option shown to the player")
    public record Choice(
            @Schema(example = "7b8e8c2f-3d1b-4a92-8c0a-0f1d2e3c4b5a")
            String choiceId,
            String text
    ) {}
}
