package com.example.demo.entity;

import com.example.demo.enums.QuizType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz")
@Getter
@Setter
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    private Integer questionCount;

    private String difficultyLevel;

    @Column(columnDefinition = "JSONB")
    private String quizJson;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
