package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.dto.question.QuestionCreateDto;
import org.example.brainbuster.dto.question.QuestionReadDto;
import org.example.brainbuster.dto.question.QuestionUpdateDto;
import org.example.brainbuster.model.IncorrectAnswer;
import org.example.brainbuster.model.Question;
import org.example.brainbuster.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<QuestionReadDto> getQuestionByIdDto(Long id) {
        // use findById(...) if you used @EntityGraph; otherwise use findByIdWithAnswers(...)
        return questionRepository.findById(id)
                .map(this::toReadDto);
    }

    @Transactional
    public Question createQuestion(QuestionCreateDto dto) {
        // Basic sanity guards (avoid duplicates, avoid equal to correct)
        var wrongs = new LinkedHashSet<String>();
        for (String s : dto.incorrectAnswers()) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty() && !trimmed.equals(dto.correctAnswer())) {
                wrongs.add(trimmed);
            }
        }
        if (wrongs.isEmpty()) {
            throw new IllegalArgumentException("At least one incorrect answer different from the correct answer is required.");
        }

        Question q = new Question();
        q.setType(dto.type().trim());
        q.setDifficulty(dto.difficulty().trim());
        q.setCategory(dto.category().trim());
        q.setQuestion(dto.question().trim());
        q.setCorrectAnswer(dto.correctAnswer().trim());

        for (String wa : wrongs) {
            IncorrectAnswer ia = new IncorrectAnswer();
            ia.setText(wa);
            q.addIncorrectAnswer(ia);
        }

        return questionRepository.save(q);
    }

    @Transactional
    public Question updateQuestion(Long id, QuestionUpdateDto dto) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question "+id+" not found"));

        q.setType(dto.type().trim());
        q.setDifficulty(dto.difficulty().trim());
        q.setCategory(dto.category().trim());
        q.setQuestion(dto.question().trim());
        q.setCorrectAnswer(dto.correctAnswer().trim());

        var it = q.getIncorrectAnswers().iterator();
        while (it.hasNext()) {
            var ia = it.next();
            it.remove();
            ia.setQuestion(null);
        }

        if (dto.incorrectAnswers() != null) {
            for (String raw : dto.incorrectAnswers()) {
                String t = (raw == null) ? null : raw.trim();
                boolean valid = t != null
                        && !t.isEmpty()
                        && !t.equals(dto.correctAnswer());

                if (valid) {
                    IncorrectAnswer ia = new IncorrectAnswer();
                    ia.setText(t);
                    q.addIncorrectAnswer(ia); // sets both sides
                }
            }
        }

        return questionRepository.save(q); // cascades MERGE/PERSIST
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Question " + id + " not found"));
        questionRepository.delete(q);
    }

    @Transactional(readOnly = true)
    public Page<QuestionReadDto> search(String category, String difficulty, String type, String q,
                                        int page, int size, String sort) {
        Sort s;
        if (sort == null || sort.isBlank()) {
            s = Sort.by(Sort.Direction.ASC, "id");
        } else {
            boolean desc = sort.startsWith("-");
            Sort.Direction dir = desc ? Sort.Direction.DESC : Sort.Direction.ASC;
            String prop = desc ? sort.substring(1) : sort;
            s = Sort.by(dir, prop);
        }

        Page<Question> found = questionRepository.search(
                nullIfBlank(category),
                nullIfBlank(difficulty),
                nullIfBlank(type),
                nullIfBlank(q),
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), s)
        );

        return found.map(this::toReadDto);
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public QuestionReadDto toReadDto(Question q) {
        var wrongs = q.getIncorrectAnswers()
                .stream().map(IncorrectAnswer::getText).toList();
        return new QuestionReadDto(
                q.getId(), q.getType(), q.getDifficulty(), q.getCategory(),
                q.getQuestion(), q.getCorrectAnswer(), wrongs
        );
    }

}