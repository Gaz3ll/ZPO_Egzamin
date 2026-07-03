package com.elearning.controller;

import com.elearning.dto.QuizSubmitRequest;
import com.elearning.dto.QuizResultResponse;
import com.elearning.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@Tag(name = "Quiz", description = "Quiz submission and grading API")
public class QuizApiController {

    private final QuizService quizService;

    public QuizApiController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz answers", description = "Submit answers and receive score percentage")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizResultResponse> submitAnswers(Authentication auth, @RequestBody QuizSubmitRequest request) {
        QuizResultResponse result = quizService.submitAnswers(auth.getName(), request);
        return ResponseEntity.ok(result);
    }
}
