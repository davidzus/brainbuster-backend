package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.model.IncorrectAnswer;
import org.example.brainbuster.repository.IncorrectAnswerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncorrectAnswerService {
    private final IncorrectAnswerRepository incorrectAnswerRepository;

    public List<IncorrectAnswer> getAllIncorrectAnswers() {
        return incorrectAnswerRepository.findAll();
    }

    public Optional<IncorrectAnswer> getIncorrectAnswerById(Long id) {
        return incorrectAnswerRepository.findById(id);
    }

    public IncorrectAnswer createIncorrectAnswer(IncorrectAnswer incorrectAnswer) {
        return incorrectAnswerRepository.save(incorrectAnswer);
    }

    public IncorrectAnswer updateIncorrectAnswer(IncorrectAnswer incorrectAnswer) {
        return incorrectAnswerRepository.save(incorrectAnswer);
    }

    public void deleteIncorrectAnswer(Long id) {
        incorrectAnswerRepository.deleteById(id);
    }
}