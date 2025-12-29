package com.example.demo.service;

import com.example.demo.entity.Document;
import com.example.demo.entity.Quiz;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.repository.QuizRepository;
import com.example.demo.request.OllamaRequest;
import com.example.demo.request.QuizGenerationRequest;
import com.example.demo.request.QuizQuestion;
import com.example.demo.response.OllamaResponse;
import com.example.demo.util.ChunkUtil;
import com.example.demo.util.QuizUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizGenerationService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final DocumentRepository documentRepository;
    private final QuizRepository quizRepository;
    private final QuizUtil quizUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ChunkUtil chunkUtil;

    public List<QuizQuestion> generateQuiz(QuizGenerationRequest req) throws Exception {
        Document doc = documentRepository.findById(req.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        String content = Optional.ofNullable(doc.getContent())
                .filter(c -> !c.isBlank())
                .orElseThrow(() -> new IllegalStateException("Document content is empty"));

        List<String> chunks = chunkUtil.chunkContent(content);
        if (chunks.isEmpty()) throw new IllegalStateException("No valid text chunks generated");

        int totalRequired = req.getQuestionCount();
        int chunkCount = Math.min(chunks.size(), totalRequired);
        int basePerChunk = totalRequired / chunkCount;
        int remainder = totalRequired % chunkCount;

        List<CompletableFuture<List<QuizQuestion>>> futures = new ArrayList<>();

        for (int i = 0; i < chunkCount; i++) {
            int quota = basePerChunk + (i < remainder ? 1 : 0);
            String chunkText = quizUtil.truncateChunk(chunks.get(i), 1500);

            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return generateQuestionsFromChunk(chunkText, req, quota);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }));
        }
        List<QuizQuestion> allQuestions = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .limit(totalRequired)
                .collect(Collectors.toList());
        return allQuestions;
    }

    private List<QuizQuestion> generateQuestionsFromChunk(String chunk, QuizGenerationRequest req, int questionCount) throws Exception {
        String prompt = buildPrompt(chunk, req, questionCount);

        OllamaRequest ollamaReq =
                OllamaRequest.builder().prompt(prompt)
                .model("llama3.2:3b")
                .stream(true)
                .build();


        OllamaResponse resp = restTemplate.postForObject(OLLAMA_URL, ollamaReq, OllamaResponse.class);

        if (resp == null || resp.getResponse() == null || resp.getResponse().isBlank())
            return Collections.emptyList();

        return quizUtil.parseQuizQuestionsSafely(resp.getResponse());
    }

    private void saveQuiz(Document doc, QuizGenerationRequest req, List<QuizQuestion> questions) throws JsonProcessingException {

        Quiz quiz = new Quiz();
        quiz.setDocument(doc);
        quiz.setQuizType(req.getQuizType());
        quiz.setQuestionCount(questions.size());
        quiz.setDifficultyLevel(req.getDifficulty());
        quiz.setQuizJson(objectMapper.writeValueAsString(questions));

        quizRepository.save(quiz);
    }


    private String buildPrompt(String content, QuizGenerationRequest req, int questionCountForThisCall) {

        String typeInstruction = switch (req.getQuizType()) {
            case SINGLE_CHOICE -> "Mỗi câu có 4 đáp án A,B,C,D và CHỈ 1 đáp án đúng";
            case MULTIPLE_CHOICE -> "Mỗi câu có 4 đáp án A,B,C,D và CÓ THỂ NHIỀU đáp án đúng (correctAnswer là mảng)";
            case TRUE_FALSE -> "Mỗi câu là dạng ĐÚNG / SAI, correctAnswer là true hoặc false";
            case ESSAY -> "Mỗi câu là câu hỏi TỰ LUẬN, KHÔNG có options và correctAnswer";
        };

        return """
                Bạn là hệ thống tạo đề thi.
                
                YÊU CẦU BẮT BUỘC:
                - Chỉ tạo ĐÚNG %d câu hỏi
                - Không tạo nhiều hơn
                - Không tạo ít hơn
                
                Loại câu hỏi: %s
                %s
                Độ khó: %s
                
                FORMAT DUY NHẤT (JSON ARRAY):
                
                [
                  {
                    "question": "...",
                    "options": {
                      "A": "...",
                      "B": "...",
                      "C": "...",
                      "D": "..."
                    },
                    "correctAnswer": "A"
                  }
                ]
                
                QUY ĐỊNH NGHIÊM NGẶT:
                - Chỉ trả về JSON
                - Không markdown
                - Không giải thích
                - Không text thừa
                - Bám sát nội dung tài liệu
                
                NỘI DUNG TÀI LIỆU:
                %s
                """.formatted(questionCountForThisCall, req.getQuizType().name(), typeInstruction, req.getDifficulty() != null ? req.getDifficulty() : "trung bình", content);
    }
}
