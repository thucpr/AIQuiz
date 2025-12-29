package com.example.demo.service;

import com.example.demo.entity.Document;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.util.FileTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadFileService {
    private final FileTextExtractor fileTextExtractor;
    private final DocumentRepository documentRepository;

    public void uploadFile(MultipartFile file) throws Exception {
        String content = fileTextExtractor.extractText(file);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("File don't have content");
        }
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContent(content);
        documentRepository.save(document);
    }
}
