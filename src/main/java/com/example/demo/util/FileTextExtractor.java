package com.example.demo.util;//package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Collectors;

@Service
public class FileTextExtractor {

   public String extractText(MultipartFile file) throws Exception {
       String filename = file.getOriginalFilename();

      if (filename.endsWith(".pdf")) {
          return extractPdf(file);
        } else if (filename.endsWith(".docx")) {
           return extractDocx(file);
       } else {
           return new String(file.getBytes());
       }
    }
    private String extractPdf(MultipartFile file) throws Exception {
       PDDocument document = PDDocument.load(file.getInputStream());
       PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }

    private String extractDocx(MultipartFile file) throws Exception {
        XWPFDocument doc = new XWPFDocument(file.getInputStream());
        return doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .collect(Collectors.joining("\n"));
    }
}
