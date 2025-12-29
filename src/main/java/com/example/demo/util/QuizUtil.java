package com.example.demo.util;

import com.example.demo.request.QuizQuestion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

// QuizUtil.java
@Service
public class QuizUtil {

    @Autowired
    private ObjectMapper objectMapper;

    // Hàm cũ parseQuizQuestionsSafely
    public List<QuizQuestion> parseQuizQuestionsSafely(String raw) throws Exception {
        raw = raw.trim();
        if (raw.startsWith("[")) {
            return objectMapper.readValue(
                    raw,
                    new TypeReference<List<QuizQuestion>>() {}
            );
        }
        if (raw.startsWith("{")) {
            JsonNode root = objectMapper.readTree(raw);
            if (root.has("questions")) {
                return objectMapper.readValue(
                        root.get("questions").toString(),
                        new TypeReference<List<QuizQuestion>>() {}
                );
            }
            if (root.has("question")) {
                QuizQuestion q = objectMapper.treeToValue(root, QuizQuestion.class);
                return List.of(q);
            }
        }
        int start = raw.indexOf("[");
        int end = raw.lastIndexOf("]");
        if (start != -1 && end != -1 && start < end) {
            String json = raw.substring(start, end + 1);
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<QuizQuestion>>() {}
            );
        }
        throw new RuntimeException("cannot parse JSON from AI");
    }

    // Hàm mới combine các chunk NDJSON stream của Ollama
    public String combineOllamaNDJSON(String raw) {
        StringBuilder sb = new StringBuilder();

        for (String line : raw.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            try {
                JsonNode node = objectMapper.readTree(line);
                if (node.has("response")) {
                    sb.append(node.get("response").asText());
                }
            } catch (JsonProcessingException e) {
                // Một số chunk không phải JSON hoàn chỉnh → bỏ qua
                continue;
            }
        }

        return sb.toString();
    }

    // Kết hợp luôn parse từ NDJSON
    public List<QuizQuestion> parseOllamaStreamNDJSON(String raw) throws Exception {
        String combined = combineOllamaNDJSON(raw).trim();
        if (combined.isEmpty()) {
            return Collections.emptyList();
        }
        return parseQuizQuestionsSafely(combined);
    }


    // QuizUtil.java

        // Hàm truncateChunk
        public String truncateChunk(String text, int maxLength) {
            if (text == null) return "";
            text = text.trim();
            if (text.length() <= maxLength) return text;

            // Cắt từ đầu đến maxLength
            String truncated = text.substring(0, maxLength);

            // Cố gắng cắt tại dấu câu cuối cùng để không cắt nửa câu
            int lastDot = truncated.lastIndexOf(".");
            if (lastDot > maxLength / 2) { // nếu tìm thấy dấu "." ở nửa sau
                truncated = truncated.substring(0, lastDot + 1);
            }

            return truncated;
        }
}
