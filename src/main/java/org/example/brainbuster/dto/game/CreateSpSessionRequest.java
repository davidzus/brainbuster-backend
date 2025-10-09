package org.example.brainbuster.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Start parameters for a single-player session")
public record CreateSpSessionRequest(
        @Schema(example = "10") @Min(1) @Max(100) int numQuestions,
        @Schema(example = "General Knowledge") String category,
        @Schema(example = "EASY") String difficulty
) {}
