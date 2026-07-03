package com.elearning.service;

import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    private static final double CORRECT_POINTS = 1.0;
    private static final double WRONG_PENALTY = -0.5;
    private static final double SKIP_POINTS = 0.0;
    private static final double PASS_THRESHOLD = 0.5;

    public double calculateScore(int totalQuestions, int correctCount, int wrongCount) {
        return correctCount * CORRECT_POINTS + wrongCount * WRONG_PENALTY + (totalQuestions - correctCount - wrongCount) * SKIP_POINTS;
    }

    public double calculatePercentage(double score, int totalQuestions) {
        double maxScore = totalQuestions * CORRECT_POINTS;
        return Math.max(0, (score / maxScore) * 100.0);
    }

    public boolean isPassed(double percentage) {
        return percentage > PASS_THRESHOLD * 100;
    }
}
