package org.example.brainbuster;

import org.example.brainbuster.controller.IncorrectAnswerController;
import org.example.brainbuster.model.IncorrectAnswer;
import org.example.brainbuster.service.IncorrectAnswerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IncorrectAnswerControllerTest {

    @Mock
    private IncorrectAnswerService incorrectAnswerService;

    @InjectMocks
    private IncorrectAnswerController incorrectAnswerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllIncorrectAnswers_shouldReturnList() {
        List<IncorrectAnswer> answers = Arrays.asList(
                new IncorrectAnswer(1L, "Wrong1", null),
                new IncorrectAnswer(2L, "Wrong2", null)
        );
        when(incorrectAnswerService.getAllIncorrectAnswers()).thenReturn(answers);

        List<IncorrectAnswer> result = incorrectAnswerController.getAllIncorrectAnswers();

        assertEquals(2, result.size());
        verify(incorrectAnswerService).getAllIncorrectAnswers();
    }

    @Test
    void getIncorrectAnswerById_existingId_shouldReturnAnswer() {
        Long id = 1L;
        IncorrectAnswer answer = new IncorrectAnswer(id, "Wrong Answer", null);
        when(incorrectAnswerService.getIncorrectAnswerById(id)).thenReturn(Optional.of(answer));

        ResponseEntity<IncorrectAnswer> response = incorrectAnswerController.getIncorrectAnswerById(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(answer, response.getBody());
        verify(incorrectAnswerService).getIncorrectAnswerById(id);
    }

    @Test
    void getIncorrectAnswerById_nonExistingId_shouldReturn404() {
        Long id = 99L;
        when(incorrectAnswerService.getIncorrectAnswerById(id)).thenReturn(Optional.empty());

        ResponseEntity<IncorrectAnswer> response = incorrectAnswerController.getIncorrectAnswerById(id);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(incorrectAnswerService).getIncorrectAnswerById(id);
    }

    @Test
    void createIncorrectAnswer_shouldReturnCreatedAnswer() {
        IncorrectAnswer input = new IncorrectAnswer(null, "New Wrong Answer", null);
        IncorrectAnswer saved = new IncorrectAnswer(1L, "New Wrong Answer", null);
        when(incorrectAnswerService.createIncorrectAnswer(input)).thenReturn(saved);

        IncorrectAnswer result = incorrectAnswerController.createIncorrectAnswer(input);

        assertEquals(saved, result);
        verify(incorrectAnswerService).createIncorrectAnswer(input);
    }

    @Test
    void updateIncorrectAnswer_existingId_shouldReturnUpdatedAnswer() {
        Long id = 1L;
        IncorrectAnswer input = new IncorrectAnswer(null, "Updated", null);
        IncorrectAnswer updated = new IncorrectAnswer(id, "Updated", null);

        when(incorrectAnswerService.updateIncorrectAnswer(any())).thenReturn(updated);

        ResponseEntity<IncorrectAnswer> response = incorrectAnswerController.updateIncorrectAnswer(id, input);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updated, response.getBody());
        assertEquals(id, input.getId());  // Sicherstellen, dass ID gesetzt wurde
        verify(incorrectAnswerService).updateIncorrectAnswer(input);
    }

    @Test
    void deleteIncorrectAnswer_shouldReturnOk() {
        Long id = 1L;

        // Keine Exception -> erfolgreich
        ResponseEntity<Void> response = incorrectAnswerController.deleteIncorrectAnswer(id);

        assertEquals(200, response.getStatusCode().value());
        verify(incorrectAnswerService).deleteIncorrectAnswer(id);
    }
}
