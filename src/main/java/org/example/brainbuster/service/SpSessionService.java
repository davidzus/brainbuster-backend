package org.example.brainbuster.service;

import org.example.brainbuster.dto.game.*;
import org.example.brainbuster.dto.question.QuestionReadDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SpSessionService {

    private final QuestionService questionService;               // <— use your service
    private final Map<UUID, Session> store = new ConcurrentHashMap<>();

    public SpSessionService(QuestionService questionService) {
        this.questionService = questionService;
    }

    // -------- Small records used by the controller summary --------
    public record Created(UUID id, SessionState state, int totalQuestions) {}
    public record Summary(UUID id, SessionState state, int currentIndex, int totalQuestions, int answered, int correctAnswers) {}

    // -------- In-memory domain for v1 --------
    private static final class Session {
        final UUID id = UUID.randomUUID();
        SessionState state = SessionState.CREATED;
        final List<Sq> questions;   // fixed list of derived session-questions
        int cur = 0;                // next question index
        int correctCount = 0;

        Session(List<Sq> questions) { this.questions = questions; }
        int total() { return questions.size(); }
        boolean finished() { return state == SessionState.FINISHED; }
    }

    private static final class Sq {
        final long questionId;
        final String prompt;
        final List<Choice> choices;        // shuffled, opaque ids
        final String correctChoiceId;      // server-only truth
        boolean answered = false;
        Sq(long questionId, String prompt, List<Choice> choices, String correctChoiceId) {
            this.questionId = questionId; this.prompt = prompt; this.choices = choices; this.correctChoiceId = correctChoiceId;
        }
        static final class Choice { final String id; final String text; Choice(String id, String text){ this.id=id; this.text=text; } }
    }

    // -------- Public API used by controller --------
    public Created create(CreateSpSessionRequest req) {
        var page = questionService.search(
                nullIfBlank(req.category()),
                nullIfBlank(req.difficulty()),
                null,
                null,
                0,
                1000,
                null
        );

        // Make it mutable before shuffle:
        List<QuestionReadDto> pool = new ArrayList<>(page.getContent());

        if (pool.size() < req.numQuestions()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Not enough questions: requested=" + req.numQuestions() + ", available=" + pool.size()
            );
        }

        // Optional micro-opt: skip shuffle if only 1
        if (pool.size() > 1) {
            Collections.shuffle(pool);
        }

        List<QuestionReadDto> picked = pool.subList(0, req.numQuestions());
        List<Sq> sqs = picked.stream().map(this::toSessionQuestion).collect(Collectors.toList());

        Session s = new Session(sqs);
        store.put(s.id, s);
        return new Created(s.id, s.state, s.total());
    }

    public QuestionPayload start(UUID id) {
        Session s = get(id);
        if (s.finished()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Session finished");
        if (s.state == SessionState.RUNNING) return asPayload(s, s.cur); // idempotent
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

    // -------- helpers --------
    private Session get(UUID id) {
        Session s = store.get(id);
        if (s == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        return s;
    }

    private Sq toSessionQuestion(QuestionReadDto dto) {
        List<Sq.Choice> choices = new ArrayList<>();
        String correctId = UUID.randomUUID().toString();

        // 1) correct
        choices.add(new Sq.Choice(correctId, dto.correctAnswer()));

        // 2) incorrects (strings in DTO)
        for (String wa : dto.incorrectAnswers()) {
            if (wa == null || wa.isBlank()) continue;
            choices.add(new Sq.Choice(UUID.randomUUID().toString(), wa));
        }

        // Shuffle presentation; server keeps the correctChoiceId
        Collections.shuffle(choices);
        return new Sq(dto.id(), dto.question(), choices, correctId);
    }

    private QuestionPayload asPayload(Session s, int index) {
        Sq q = s.questions.get(index);
        var out = q.choices.stream()
                .map(c -> new QuestionPayload.Choice(c.id, c.text))
                .toList();
        return new QuestionPayload(q.questionId, q.prompt, out, index, s.total());
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
