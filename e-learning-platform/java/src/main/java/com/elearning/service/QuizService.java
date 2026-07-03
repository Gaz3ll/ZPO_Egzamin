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
        int correct = 0, wrong = 0;

        for (Question q : questions) {
            String answer = request.getAnswers().get(q.getId().toString());
            if (answer == null || answer.isEmpty()) continue;
            if (answer.equals(q.getCorrectAnswer())) correct++;
            else wrong++;
        }

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
