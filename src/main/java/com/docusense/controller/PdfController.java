package com.docusense.controller;

import com.docusense.service.PdfExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class PdfController {

    @Autowired
    private PdfExtractorService pdfExtractorService;

    // POST /api/upload
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            String extractedText = pdfExtractorService.extractText(file);
            return ResponseEntity.ok(extractedText);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to extract text: " + e.getMessage());
        }
    }
}