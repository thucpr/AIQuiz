package com.example.demo.controller;

import com.example.demo.request.QuizGenerationRequest;
import com.example.demo.request.QuizQuestion;
import com.example.demo.service.QuizGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizGenerationService quizGenerationService;

    public QuizController(QuizGenerationService quizGenerationService) {
        this.quizGenerationService = quizGenerationService;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<QuizQuestion>> generateQuiz(
            @RequestBody QuizGenerationRequest request
    ) throws Exception {
        List<QuizQuestion> quizQuestionList = quizGenerationService.generateQuiz(request);
        return ResponseEntity.ok(quizQuestionList);
    }
}
