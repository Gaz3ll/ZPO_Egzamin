package com.elearning.controller;

import com.elearning.model.QuizResult;
import com.elearning.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teacher")
@Tag(name = "Teacher", description = "Teacher-only endpoints")
public class TeacherController {

    private final QuizService quizService;

    public TeacherController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('TEACHER')")
    public String viewResults(Model model) {
        List<QuizResult> results = quizService.getAllResults();
        model.addAttribute("results", results);
        return "results";
    }

    @GetMapping("/api/results")
    @ResponseBody
    @Operation(summary = "Get all student results", description = "Teacher views all results")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<QuizResult>> getAllResults() {
        return ResponseEntity.ok(quizService.getAllResults());
    }
}
