package com.elearning.controller;

import com.elearning.dto.QuizSubmitRequest;
import com.elearning.dto.QuizResultResponse;
import com.elearning.model.Question;
import com.elearning.service.QuizService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public String showQuiz(Model model) {
        List<Question> questions = quizService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "quiz";
    }

    @PostMapping("/submit")
    public String submitQuiz(Authentication auth, @RequestParam java.util.Map<String, String> params, Model model) {
        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(params);
        QuizResultResponse result = quizService.submitAnswers(auth.getName(), request);
        model.addAttribute("result", result);
        return "summary";
    }
}
