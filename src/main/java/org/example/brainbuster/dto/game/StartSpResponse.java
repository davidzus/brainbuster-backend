package org.example.brainbuster.dto.game;

public record StartSpResponse(
        SessionState state,
        QuestionPayload current
) {}
