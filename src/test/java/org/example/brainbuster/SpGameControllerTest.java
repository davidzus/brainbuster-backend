package org.example.brainbuster;

import org.example.brainbuster.controller.SpGameController;
import org.example.brainbuster.dto.game.*;
import org.example.brainbuster.dto.game.QuestionPayload.Choice;
import org.example.brainbuster.service.SpSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpGameControllerTest {

    @Mock
    private SpSessionService service;

    @InjectMocks
    private SpGameController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldReturnCreatedSession_withPrincipal() {
        CreateSpSessionRequest request = new CreateSpSessionRequest(5, "General Knowledge", "EASY");
        UUID sessionId = UUID.randomUUID();
        var created = new SpSessionService.Created(sessionId, SessionState.CREATED, 5);

        var principal = mock(org.springframework.security.core.userdetails.UserDetails.class);
        when(principal.getUsername()).thenReturn("alice");

        when(service.create(request, "alice")).thenReturn(created);

        ResponseEntity<CreateSpSessionResponse> response = controller.create(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sessionId, response.getBody().sessionId());
        verify(service).create(request, "alice");
        verifyNoMoreInteractions(service);
    }

    @Test
    void create_whenNotEnoughQuestions_shouldThrow422() {
        CreateSpSessionRequest request = new CreateSpSessionRequest(99, "General Knowledge", "EASY");

        var principal = mock(org.springframework.security.core.userdetails.UserDetails.class);
        when(principal.getUsername()).thenReturn("alice");

        when(service.create(request, "alice"))
                .thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThrows(ResponseStatusException.class, () -> controller.create(principal, request));

        verify(service).create(request,"alice");
        verifyNoMoreInteractions(service);
    }

    @Test
    void start_shouldReturnFirstQuestion() {
        UUID sessionId = UUID.randomUUID();
        List<Choice> choices = List.of(new Choice("",""), new Choice("",""), new Choice("",""),new Choice("",""));
        QuestionPayload question = new QuestionPayload(1L, "2+2=?", choices, 0, 5);

        when(service.start(sessionId)).thenReturn(question);

        StartSpResponse response = controller.start(sessionId);

        assertEquals(SessionState.RUNNING, response.state());
        assertEquals(question, response.current());
    }

    @Test
    void start_finishedSession_shouldThrow409() {
        UUID id = UUID.randomUUID();
        when(service.start(id)).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Session finished"));

        assertThrows(ResponseStatusException.class, () -> controller.start(id));
    }

    @Test
    void current_shouldReturnCurrentQuestion() {
        UUID id = UUID.randomUUID();
        List<Choice> choices = List.of(new Choice("",""),new Choice("",""),new Choice("",""),new Choice("",""));
        QuestionPayload question = new QuestionPayload(1L, "2+3=?", choices, 1, 5);
        when(service.current(id)).thenReturn(question);

        QuestionPayload result = controller.current(id);

        assertEquals(question, result);
        verify(service).current(id);
    }

    @Test
    void current_sessionNotRunning_shouldThrow409() {
        UUID id = UUID.randomUUID();
        when(service.current(id)).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Session not running"));

        assertThrows(ResponseStatusException.class, () -> controller.current(id));
    }

    @Test
    void answer_alreadyAnswered_shouldThrow409() {
        UUID id = UUID.randomUUID();
        String choiceId = UUID.randomUUID().toString();
        AnswerRequest request = new AnswerRequest(choiceId);
        when(service.answer(id, choiceId))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Already answered"));

        assertThrows(ResponseStatusException.class, () -> controller.answer(id, request));
    }

    @Test
    void summary_shouldReturnSessionSummary() {
        UUID id = UUID.randomUUID();
        var summary = new SpSessionService.Summary(id, SessionState.FINISHED, 10, 10, 10, 9);
        when(service.summary(id)).thenReturn(summary);

        SessionSummary result = controller.summary(id);

        assertEquals(id, result.sessionId());
        assertEquals(SessionState.FINISHED, result.state());
        assertEquals(10, result.currentIndex());
        assertEquals(9, result.correctAnswers());
    }

    @Test
    void summary_sessionNotFound_shouldThrow404() {
        UUID id = UUID.randomUUID();
        when(service.summary(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThrows(ResponseStatusException.class, () -> controller.summary(id));
    }
}
