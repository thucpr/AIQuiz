package com.example.demo.request;

import com.example.demo.enums.QuizType;
import lombok.Data;

import java.util.UUID;

@Data
public class QuizGenerationRequest {

    private UUID documentId;
    private int questionCount;
    private QuizType quizType;
    private String difficulty;
    private boolean shuffleOptions = true;
}

