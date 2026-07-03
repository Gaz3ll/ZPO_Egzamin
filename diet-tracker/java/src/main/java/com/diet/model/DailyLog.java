package com.diet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "daily_logs")
public class DailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String date; // YYYY-MM-DD
    private Long mealId;
    private String userId;

    public DailyLog() {}

    public DailyLog(String date, Long mealId, String userId) {
        this.date = date;
        this.mealId = mealId;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Long getMealId() { return mealId; }
    public void setMealId(Long mealId) { this.mealId = mealId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
