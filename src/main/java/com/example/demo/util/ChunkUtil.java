package com.example.demo.util;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkUtil {

    private static final int CHUNK_SIZE = 1000;

    public List<String> chunkContent(String content) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = content.split("(?<=[.!?])\\s+");
        StringBuilder chunk = new StringBuilder();

        for (String sentence : sentences) {
            if (chunk.length() + sentence.length() > CHUNK_SIZE) {
                chunks.add(chunk.toString().trim());
                chunk = new StringBuilder();
            }
            chunk.append(sentence).append(" ");
        }
        if (chunk.length() > 0) {
            chunks.add(chunk.toString().trim());
        }
        return chunks;
    }
}
