package org.example.brainbuster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.question.QuestionCreateDto;
import org.example.brainbuster.dto.question.QuestionReadDto;
import org.example.brainbuster.model.Question;
import org.example.brainbuster.dto.question.QuestionUpdateDto;
import org.example.brainbuster.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionReadDto> getQuestionById(@PathVariable("id") Long id) {
        return questionService.getQuestionByIdDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public QuestionReadDto createQuestion(@Valid @RequestBody QuestionCreateDto body) {
        var saved = questionService.createQuestion(body);
        return questionService.toReadDto(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionReadDto> updateQuestion(
            @PathVariable("id") Long id,
            @Valid @RequestBody QuestionUpdateDto body) {
        try {
            var saved = questionService.updateQuestion(id, body);
            return ResponseEntity.ok(questionService.toReadDto(saved));
        } catch (IllegalArgumentException notFound) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("id") Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();  // 404
        }
    }
}