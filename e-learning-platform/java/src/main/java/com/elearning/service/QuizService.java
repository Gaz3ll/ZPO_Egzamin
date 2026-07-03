package com.elearning.service;

import com.elearning.dto.QuizSubmitRequest;
import com.elearning.dto.QuizResultResponse;
import com.elearning.model.Question;
import com.elearning.model.QuizResult;
import com.elearning.repository.QuestionRepository;
import com.elearning.repository.QuizResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final ScoreCalculator scoreCalculator;

    public QuizService(QuestionRepository questionRepository, QuizResultRepository quizResultRepository, ScoreCalculator scoreCalculator) {
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.scoreCalculator = scoreCalculator;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public QuizResultResponse submitAnswers(String userId, QuizSubmitRequest request) {
        List<Question> questions = questionRepository.findAll();
        List<Question> answered = questions.stream()
            .filter(q -> request.getAnswers().get(q.getId().toString()) != null && !request.getAnswers().get(q.getId().toString()).isEmpty())
            .toList();

        int correct = (int) answered.stream()
            .filter(q -> request.getAnswers().get(q.getId().toString()).equals(q.getCorrectAnswer()))
            .count();

        int wrong = answered.size() - correct;

        double score = scoreCalculator.calculateScore(questions.size(), correct, wrong);
        double percentage = scoreCalculator.calculatePercentage(score, questions.size());
        boolean passed = scoreCalculator.isPassed(percentage);

        QuizResult result = new QuizResult(userId, score, percentage, passed);
        quizResultRepository.save(result);

        return new QuizResultResponse(score, percentage, passed, correct, wrong, questions.size());
    }

    public List<QuizResult> getAllResults() {
        return quizResultRepository.findAll();
    }
}
