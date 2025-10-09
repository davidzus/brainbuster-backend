package org.example.brainbuster.dto.game;

import java.util.UUID;

public record SessionSummary(
        UUID sessionId,
        SessionState state,
        int currentIndex,
        int totalQuestions,
        int answered,
        int correctAnswers
) {}
