package org.example.brainbuster.controller;

import org.example.brainbuster.dto.game.*;
import org.example.brainbuster.service.SpSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/sp/sessions")
@Validated
public class SpGameController {

    private final SpSessionService service;

    public SpGameController(SpSessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateSpSessionResponse> create(@Valid @RequestBody CreateSpSessionRequest req) {
        var s = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateSpSessionResponse(s.id(), s.state(), s.totalQuestions()));
    }

    @PostMapping("/{id}/start")
    public StartSpResponse start(@PathVariable UUID id) {
        var q = service.start(id);
        return new StartSpResponse(SessionState.RUNNING, q);
    }

    @GetMapping("/{id}/current")
    public QuestionPayload current(@PathVariable UUID id) {
        return service.current(id);
    }

    @PostMapping("/{id}/answer")
    public AnswerResponse answer(@PathVariable UUID id, @Valid @RequestBody AnswerRequest req) {
        return service.answer(id, req.choiceId());
    }

    @GetMapping("/{id}")
    public SessionSummary summary(@PathVariable UUID id) {
        var s = service.summary(id);
        return new SessionSummary(s.id(), s.state(), s.currentIndex(), s.totalQuestions(), s.answered(), s.correctAnswers());
    }
}