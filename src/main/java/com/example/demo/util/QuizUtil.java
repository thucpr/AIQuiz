package com.example.demo.util;

import com.example.demo.request.QuizQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QuizUtil {

    private final ObjectMapper objectMapper;

    public List<QuizQuestion> parseQuizQuestionsSafely(String raw) throws Exception {

        raw = raw.trim();
        if (raw.startsWith("[")) {
            return objectMapper.readValue(
                    raw,
                    new TypeReference<List<QuizQuestion>>() {
                    }
            );
        }
        if (raw.startsWith("{")) {
            JsonNode root = objectMapper.readTree(raw);

            if (root.has("questions")) {
                return objectMapper.readValue(
                        root.get("questions").toString(),
                        new TypeReference<List<QuizQuestion>>() {
                        }
                );
            }

            if (root.has("question")) {
                QuizQuestion q =
                        objectMapper.treeToValue(root, QuizQuestion.class);
                return List.of(q);
            }
        }
        int start = raw.indexOf("[");
        int end = raw.lastIndexOf("]");

        if (start != -1 && end != -1 && start < end) {
            String json = raw.substring(start, end + 1);
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<QuizQuestion>>() {
                    }
            );
        }

        throw new RuntimeException("can not pare JSON from AI");
    }

    public String truncateChunk(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}
