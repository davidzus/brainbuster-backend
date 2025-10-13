package org.example.brainbuster.service;

import org.example.brainbuster.dto.game.*;
import org.example.brainbuster.dto.question.QuestionReadDto;
import org.example.brainbuster.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SpSessionService {

    private final QuestionService questionService;
    private final UserRepository userRepository;
    private final Map<UUID, Session> store = new ConcurrentHashMap<>();

    public SpSessionService(QuestionService questionService, UserRepository userRepository) {
        this.questionService = questionService;
        this.userRepository = userRepository;
    }

    public record Created(UUID id, SessionState state, int totalQuestions) {}
    public record Summary(UUID id, SessionState state, int currentIndex, int totalQuestions, int answered, int correctAnswers) {}

    private static final class Session {
        final UUID id = UUID.randomUUID();
        final String playerUsername;
        SessionState state = SessionState.CREATED;
        final List<Sq> questions;
        int cur = 0;
        int correctCount = 0;

        Session(String playerUsername, List<Sq> questions) {
            this.playerUsername = playerUsername;
            this.questions = questions;
        }
        int total() { return questions.size(); }
        boolean finished() { return state == SessionState.FINISHED; }
    }

    private static final class Sq {
        final long questionId;
        final String prompt;
        final List<Choice> choices;
        final String correctChoiceId;
        boolean answered = false;
        Sq(long questionId, String prompt, List<Choice> choices, String correctChoiceId) {
            this.questionId = questionId; this.prompt = prompt; this.choices = choices; this.correctChoiceId = correctChoiceId;
        }
        static final class Choice {
            final String id;
            final String text;
            Choice(String id, String text) {
                this.id=id; this.text=text;
            }
        }
    }

    public Created create(CreateSpSessionRequest req, String playerUsername) {
        var page = questionService.search(
                nullIfBlank(req.category()),
                nullIfBlank(req.difficulty()),
                null, null, 0, 1000, null
        );

        List<QuestionReadDto> pool = new ArrayList<>(page.getContent());
        if (pool.size() < req.numQuestions()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Not enough questions: requested=" + req.numQuestions() + ", available=" + pool.size());
        }
        if (pool.size() > 1) Collections.shuffle(pool);
        List<QuestionReadDto> picked = pool.subList(0, req.numQuestions());

        List<Sq> sqs = picked.stream().map(this::toSessionQuestion).toList();
        Session s = new Session(playerUsername, sqs);
        store.put(s.id, s);
        return new Created(s.id, s.state, s.total());
    }

    public QuestionPayload start(UUID id) {
        Session s = get(id);
        if (s.finished()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Session finished");
        if (s.state == SessionState.RUNNING) return asPayload(s, s.cur);
        if (s.total() == 0) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No questions");
        s.state = SessionState.RUNNING;
        s.cur = 0;
        return asPayload(s, s.cur);
    }

    public QuestionPayload current(UUID id) {
        Session s = get(id);
        if (s.state != SessionState.RUNNING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Session not running");
        if (s.cur >= s.total()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Session finished");
        return asPayload(s, s.cur);
    }

    public AnswerResponse answer(UUID id, String choiceId) {
        Session s = get(id);
        if (s.state != SessionState.RUNNING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Session not running");
        if (s.cur >= s.total()) throw new ResponseStatusException(HttpStatus.CONFLICT, "No more questions");

        Sq q = s.questions.get(s.cur);
        if (q.answered) throw new ResponseStatusException(HttpStatus.CONFLICT, "Already answered");

        boolean correct = Objects.equals(q.correctChoiceId, choiceId);
        q.answered = true;
        if (correct) s.correctCount++;

        int idx = s.cur;
        int nextIdx = idx + 1;
        if (nextIdx >= s.total()) {
            s.state = SessionState.FINISHED;

            maybeUpdateHighScore(s.playerUsername, s.correctCount);

            return new AnswerResponse(correct, idx, null, s.state, null);
        } else {
            s.cur = nextIdx;
            return new AnswerResponse(correct, idx, nextIdx, s.state, asPayload(s, nextIdx));
        }
    }

    public Summary summary(UUID id) {
        Session s = get(id);
        int answered = (int) s.questions.stream().filter(q -> q.answered).count();
        return new Summary(s.id, s.state, s.cur, s.total(), answered, s.correctCount);
    }

    // --- helpers ---
    private Session get(UUID id) {
        Session s = store.get(id);
        if (s == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        return s;
    }

    private void maybeUpdateHighScore(String username, int newScore) {
        if (username == null || username.isBlank()) return;
        userRepository.findByUsername(username).ifPresent(user -> {
            int current = Optional.ofNullable(user.getHighScore()).orElse(0);
            if (newScore > current) {
                user.setHighScore(newScore);
                userRepository.save(user);
            }
        });
    }

    private Sq toSessionQuestion(QuestionReadDto dto) {
        List<Sq.Choice> choices = new ArrayList<>();
        String correctId = UUID.randomUUID().toString();
        choices.add(new Sq.Choice(correctId, dto.correctAnswer()));
        for (String wa : dto.incorrectAnswers()) {
            if (wa == null || wa.isBlank()) continue;
            choices.add(new Sq.Choice(UUID.randomUUID().toString(), wa));
        }
        Collections.shuffle(choices);
        return new Sq(dto.id(), dto.question(), choices, correctId);
    }

    private QuestionPayload asPayload(Session s, int index) {
        Sq q = s.questions.get(index);
        var out = q.choices.stream().map(c -> new QuestionPayload.Choice(c.id, c.text)).toList();
        return new QuestionPayload(q.questionId, q.prompt, out, index, s.total());
    }

    private String nullIfBlank(String s) { return (s == null || s.isBlank()) ? null : s; }
}
