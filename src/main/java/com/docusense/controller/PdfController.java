package com.docusense.controller; // This file belongs to the controller package

import java.util.List; // Import our Document model

import org.springframework.beans.factory.annotation.Autowired; // Import our DocumentService
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docusense.model.Document;
import com.docusense.service.DocumentService;

@RestController // Tells Spring this class handles HTTP requests
@RequestMapping("/api") // All endpoints start with /api
@CrossOrigin(origins = "*") // Allows requests from any origin
public class PdfController {

    @Autowired
    private DocumentService documentService; // Spring injects DocumentService automatically

    // POST /api/upload → handles single PDF upload
    @PostMapping("/upload")
    public ResponseEntity<Document> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            Document savedDocument = documentService.saveDocument(file);
            return ResponseEntity.ok(savedDocument);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/upload-bulk → handles multiple PDF uploads at once
    @PostMapping("/upload-bulk")
    public ResponseEntity<List<Document>> uploadMultiplePdfs(
            @RequestParam("files") List<MultipartFile> files) { // "files" accepts multiple files
        try {
            // Process all files and save them to database
            List<Document> savedDocuments = documentService.saveAllDocuments(files);

            // Return the list of all saved documents
            return ResponseEntity.ok(savedDocuments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}