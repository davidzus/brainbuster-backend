package org.example.brainbuster.service;

import lombok.RequiredArgsConstructor;
import org.example.brainbuster.questiondto.QuestionCreateDto;
import org.example.brainbuster.questiondto.QuestionReadDto;
import org.example.brainbuster.questiondto.QuestionUpdateDto;
import org.example.brainbuster.model.IncorrectAnswer;
import org.example.brainbuster.model.Question;
import org.example.brainbuster.repository.QuestionRepository;
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
            for (String text : dto.incorrectAnswers()) {
                if (text == null) continue;
                var t = text.trim();
                if (t.isEmpty() || t.equals(dto.correctAnswer())) continue;
                IncorrectAnswer ia = new IncorrectAnswer();
                ia.setText(t);
                q.addIncorrectAnswer(ia); // sets both sides
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

    public QuestionReadDto toReadDto(Question q) {
        var wrongs = q.getIncorrectAnswers()
                .stream().map(IncorrectAnswer::getText).toList();
        return new QuestionReadDto(
                q.getId(), q.getType(), q.getDifficulty(), q.getCategory(),
                q.getQuestion(), q.getCorrectAnswer(), wrongs
        );
    }

}