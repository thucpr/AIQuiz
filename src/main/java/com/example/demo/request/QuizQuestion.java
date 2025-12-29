package com.example.demo.request;

import lombok.Data;

import java.util.Map;
@Data
public class QuizQuestion {

    private String question;
    private Map<String, String> options;
    private Object correctAnswer;
    private String explanation;
}
