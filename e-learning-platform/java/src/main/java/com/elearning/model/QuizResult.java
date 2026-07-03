package com.elearning.model;

import jakarta.persistence.*;

@Entity
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private double score;
    private double percentage;
    private boolean passed;

    public QuizResult() {}

    public QuizResult(String userId, double score, double percentage, boolean passed) {
        this.userId = userId;
        this.score = score;
        this.percentage = percentage;
        this.passed = passed;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
}
