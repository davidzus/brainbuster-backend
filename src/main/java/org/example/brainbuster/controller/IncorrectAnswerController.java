package org.example.brainbuster.controller;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.model.IncorrectAnswer;
import org.example.brainbuster.service.IncorrectAnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/incorrect-answers")
@RequiredArgsConstructor
public class IncorrectAnswerController {
    private final IncorrectAnswerService incorrectAnswerService;

    @GetMapping
    public List<IncorrectAnswer> getAllIncorrectAnswers() {
        return incorrectAnswerService.getAllIncorrectAnswers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncorrectAnswer> getIncorrectAnswerById(@PathVariable Long id) {
        Optional<IncorrectAnswer> answer = incorrectAnswerService.getIncorrectAnswerById(id);
        return answer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public IncorrectAnswer createIncorrectAnswer(@RequestBody IncorrectAnswer incorrectAnswer) {
        return incorrectAnswerService.createIncorrectAnswer(incorrectAnswer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncorrectAnswer> updateIncorrectAnswer(@PathVariable Long id, @RequestBody IncorrectAnswer incorrectAnswer) {
        incorrectAnswer.setId(id);
        IncorrectAnswer updatedAnswer = incorrectAnswerService.updateIncorrectAnswer(incorrectAnswer);
        return ResponseEntity.ok(updatedAnswer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncorrectAnswer(@PathVariable Long id) {
        incorrectAnswerService.deleteIncorrectAnswer(id);
        return ResponseEntity.ok().build();
    }
}