package org.example.brainbuster.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record CreateSpSessionResponse(
        @Schema(example = "c0a8017b-5e32-4b1f-9a5a-2b0e4d9f0a8a")
        UUID sessionId,
        SessionState state,
        int totalQuestions
) {}
