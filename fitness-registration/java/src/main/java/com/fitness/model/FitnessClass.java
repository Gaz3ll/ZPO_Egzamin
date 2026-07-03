package com.fitness.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fitness_classes")
public class FitnessClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String dayOfWeek;
    private String time;
    private int maxCapacity;

    public FitnessClass() {}

    public FitnessClass(String type, String dayOfWeek, String time, int maxCapacity) {
        this.type = type;
        this.dayOfWeek = dayOfWeek;
        this.time = time;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
}
