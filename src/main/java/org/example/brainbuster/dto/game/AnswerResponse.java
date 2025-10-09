package org.example.brainbuster.dto.game;

public record AnswerResponse(
        boolean correct,
        int index,             // index of the answered question
        Integer nextIndex,     // null if finished
        SessionState state,    // RUNNING or FINISHED
        QuestionPayload next   // null if finished
) {}
