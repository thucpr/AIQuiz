package com.example.demo.response;


import lombok.Data;

@Data
public class OllamaResponse {
    private String model;
    private String response;
    private boolean done;
    private Long total_duration;
    private Long load_duration;
}
