package com.elearning.dto;

import java.util.Map;

public class QuizSubmitRequest {
    private Map<String, String> answers;

    public Map<String, String> getAnswers() { 
        return answers != null ? answers : java.util.Collections.emptyMap(); 
    }
    public void setAnswers(Map<String, String> answers) { this.answers = answers; }
}
