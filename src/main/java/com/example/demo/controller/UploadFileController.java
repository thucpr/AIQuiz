package com.example.demo.controller;

import com.example.demo.service.UploadFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class UploadFileController {
    private final UploadFileService uploadFileService;


    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        uploadFileService.uploadFile(file);
        return ResponseEntity.ok("Upload file successfully");
    }
}



