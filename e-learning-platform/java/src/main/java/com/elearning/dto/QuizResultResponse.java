package com.elearning.dto;

public class QuizResultResponse {
    private double score;
    private double percentage;
    private boolean passed;
    private int correct;
    private int wrong;
    private int total;

    public QuizResultResponse(double score, double percentage, boolean passed, int correct, int wrong, int total) {
        this.score = score;
        this.percentage = percentage;
        this.passed = passed;
        this.correct = correct;
        this.wrong = wrong;
        this.total = total;
    }

    public double getScore() { return score; }
    public double getPercentage() { return percentage; }
    public boolean isPassed() { return passed; }
    public int getCorrect() { return correct; }
    public int getWrong() { return wrong; }
    public int getTotal() { return total; }
}
