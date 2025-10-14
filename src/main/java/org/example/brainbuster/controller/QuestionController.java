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

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam(value = "category",   required = false) String category,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "type",       required = false) String type,
            @RequestParam(value = "q",          required = false) String freeText,
            @RequestParam(value = "page",       required = false) Integer page,
            @RequestParam(value = "size",       required = false) Integer size,
            @RequestParam(value = "sort",       required = false) String sort
    ) {
        if (page == null && size == null) {
            var results = questionService
                    .search(category, difficulty, type, freeText, 0, 1000, sort)
                    .getContent();
            return ResponseEntity.ok(results);
        } else {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            return ResponseEntity.ok(
                    questionService.search(category, difficulty, type, freeText, p, s, sort)
            );
        }
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
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}