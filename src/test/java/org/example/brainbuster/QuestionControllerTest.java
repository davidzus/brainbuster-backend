package org.example.brainbuster;

import org.example.brainbuster.controller.QuestionController;
import org.example.brainbuster.dto.question.*;
import org.example.brainbuster.model.Question;
import org.example.brainbuster.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionController questionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllQuestions_shouldReturnList() {
        List<Question> questions = Arrays.asList(new Question(), new Question());
        when(questionService.getAllQuestions()).thenReturn(questions);

        List<Question> result = questionController.getAllQuestions();

        assertEquals(2, result.size());
        verify(questionService).getAllQuestions();
    }

    @Test
    void search_noPaging_shouldReturnListOfDto() {
        List<QuestionReadDto> mockList = Arrays.asList(
            new QuestionReadDto(1L, "multiple", "easy", "science", "What is water?", "H2O", List.of("CO2", "O2", "N2")),
            new QuestionReadDto(2L, "boolean", "medium", "math", "Is 2+2=4?", "Yes", List.of("No"))
        );
        Page<QuestionReadDto> mockPage = new PageImpl<>(mockList);

        when(questionService.search(null, null, null, null, 0, 1000, null)).thenReturn(mockPage);

        ResponseEntity<?> response = questionController.search(null, null, null, null, null, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof List<?>);

        List<?> bodyList = (List<?>) response.getBody();
        assertEquals(2, bodyList.size());

        verify(questionService).search(null, null, null, null, 0, 1000, null);
    }

    @Test
    void search_withPaging_shouldReturnPageOfDto() {
        List<QuestionReadDto> mockList = Arrays.asList(
            new QuestionReadDto(1L, "multiple", "easy", "science", "What is water?", "H2O", List.of("CO2", "O2", "N2")),
            new QuestionReadDto(2L, "boolean", "medium", "math", "Is 2+2=4?", "Yes", List.of("No"))
        );
        Page<QuestionReadDto> mockPage = new PageImpl<>(mockList);

        when(questionService.search("science", "easy", "multiple", "water", 1, 5, "difficulty")).thenReturn(mockPage);

        ResponseEntity<?> response = questionController.search("science", "easy", "multiple", "water", 1, 5, "difficulty");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockPage, response.getBody());

        verify(questionService).search("science", "easy", "multiple", "water", 1, 5, "difficulty");
    }


    @Test
    void getQuestionById_existingId_shouldReturnQuestionReadDto() {
        Long id = 1L;
        QuestionReadDto dto = new QuestionReadDto(
                1L, "multiple", "easy", "science", "What is water?", "H2O",
                List.of("CO2", "O2", "N2")
        );
        when(questionService.getQuestionByIdDto(id)).thenReturn(Optional.of(dto));

        ResponseEntity<QuestionReadDto> response = questionController.getQuestionById(id);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());
        verify(questionService).getQuestionByIdDto(id);
    }

    @Test
    void getQuestionById_nonExistingId_shouldReturn404() {
        Long id = 99L;
        when(questionService.getQuestionByIdDto(id)).thenReturn(Optional.empty());

        ResponseEntity<QuestionReadDto> response = questionController.getQuestionById(id);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void createQuestion_shouldReturnCreatedQuestionReadDto() {
        QuestionCreateDto createDto = new QuestionCreateDto(
                "multiple", "medium", "math", "What is 2+2?", "4",
                List.of("3", "5", "6")
        );
        Question savedQuestion = new Question(); // Dummy
        QuestionReadDto readDto = new QuestionReadDto(
                2L, "multiple", "medium", "math", "What is 2+2?", "4",
                List.of("3", "5", "6")
        );

        when(questionService.createQuestion(createDto)).thenReturn(savedQuestion);
        when(questionService.toReadDto(savedQuestion)).thenReturn(readDto);

        QuestionReadDto result = questionController.createQuestion(createDto);

        assertEquals(readDto, result);
        verify(questionService).createQuestion(createDto);
        verify(questionService).toReadDto(savedQuestion);
    }

    @Test
    void updateQuestion_existingId_shouldReturnUpdatedReadDto() {
        Long id = 1L;
        QuestionUpdateDto updateDto = new QuestionUpdateDto(
                "boolean", "hard", "history", "Was Napoleon short?", "No",
                List.of("Yes")
        );
        Question updatedQuestion = new Question();
        QuestionReadDto readDto = new QuestionReadDto(
                id, "boolean", "hard", "history", "Was Napoleon short?", "No",
                List.of("Yes")
        );

        when(questionService.updateQuestion(id, updateDto)).thenReturn(updatedQuestion);
        when(questionService.toReadDto(updatedQuestion)).thenReturn(readDto);

        ResponseEntity<QuestionReadDto> response = questionController.updateQuestion(id, updateDto);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(readDto, response.getBody());
        verify(questionService).updateQuestion(id, updateDto);
        verify(questionService).toReadDto(updatedQuestion);
    }

    @Test
    void updateQuestion_nonExistingId_shouldReturn404() {
        Long id = 99L;
        QuestionUpdateDto updateDto = new QuestionUpdateDto(
                "boolean", "hard", "history", "Was Napoleon short?", "No",
                List.of("Yes")
        );

        when(questionService.updateQuestion(id, updateDto)).thenThrow(new IllegalArgumentException());

        ResponseEntity<QuestionReadDto> response = questionController.updateQuestion(id, updateDto);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void deleteQuestion_existingId_shouldReturnNoContent() {
        Long id = 1L;

        // Kein Throw → erfolgreich
        ResponseEntity<Void> response = questionController.deleteQuestion(id);

        assertEquals(204, response.getStatusCode().value());
        verify(questionService).deleteQuestion(id);
    }

    @Test
    void deleteQuestion_nonExistingId_shouldReturn404() {
        Long id = 99L;

        doThrow(new jakarta.persistence.EntityNotFoundException()).when(questionService).deleteQuestion(id);

        ResponseEntity<Void> response = questionController.deleteQuestion(id);

        assertEquals(404, response.getStatusCode().value());
        verify(questionService).deleteQuestion(id);
    }
}
