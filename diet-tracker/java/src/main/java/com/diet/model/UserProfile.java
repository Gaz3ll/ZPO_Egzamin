package com.diet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    private String userId;

    private double weight;
    private double height;
    private int age;
    private String gender; // M / F

    public UserProfile() {}

    public UserProfile(String userId, double weight, double height, int age, String gender) {
        this.userId = userId;
        this.weight = weight;
        this.height = height;
        this.age = age;
        this.gender = gender;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
