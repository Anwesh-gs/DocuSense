package com.docusense.service; // This file belongs to the service package

import java.io.File; // Import our Document model
import java.nio.file.Files; // Import our repository
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docusense.model.Document;
import com.docusense.model.DocumentRepository;

@Service // Tells Spring this is a service class
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository; // Handles all database operations

    @Autowired
    private PdfExtractorService pdfExtractorService; // Extracts text from PDFs

    @Autowired
    private DynamicClassificationService dynamicClassificationService; // AI dynamic categorization

    @Autowired
    private EmbeddingService embeddingService; // Generates AI embeddings for text

    @Autowired
    private DuplicateDetectionService duplicateDetectionService; // Checks for duplicate PDFs

    // Folder where uploaded PDFs will be stored on the server
    private static final String UPLOAD_DIR = "uploads/";

public DocumentResult saveDocument(MultipartFile file) throws Exception {

    // Step 1: Create uploads folder if it doesn't exist
    File uploadFolder = new File(UPLOAD_DIR);
    if (!uploadFolder.exists()) {
        uploadFolder.mkdirs();
    }

    // Step 2: Get just the filename without subfolder path
    // e.g. "demo/gas acknowledgement.pdf" → "gas acknowledgement.pdf"
    String originalFilename = new File(file.getOriginalFilename()).getName();
    String filePath = UPLOAD_DIR + originalFilename;
    Path path = Paths.get(filePath);
    Files.write(path, file.getBytes());

    // Step 3: Extract and clean text from the uploaded PDF
    String cleanedText = pdfExtractorService.extractText(file);

    // Step 4: Dynamically classify using Groq/Llama3
    String category = dynamicClassificationService.classify(cleanedText);

    // Step 5: Generate AI embedding for duplicate detection
    String embedding = embeddingService.getEmbedding(cleanedText);

    // Step 6: Create and save the document
    Document document = new Document();
    document.setFilename(originalFilename); // ← Fixed here
    document.setExtractedText(cleanedText);
    document.setCategory(category);
    document.setEmbedding(embedding);
    document.setFilePath(filePath);
    Document savedDocument = documentRepository.save(document);

// Step 7: Check for duplicates and store result
String duplicateWarning = null;
if (embedding != null) {
    duplicateWarning = duplicateDetectionService.checkDuplicate(
        embedding,
        savedDocument.getId()
    );
}

// If duplicate found → mark document as duplicate in database
if (duplicateWarning != null) {
    savedDocument.setDuplicate(true);
    documentRepository.save(savedDocument); // Save updated status
}

return new DocumentResult(savedDocument, duplicateWarning);
    
}

    // Saves multiple PDFs one by one
    public List<DocumentResult> saveAllDocuments(List<MultipartFile> files) throws Exception {

        List<DocumentResult> results = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                results.add(saveDocument(file));
            }
        }

        return results;
    }

    // Inner class to hold document and duplicate warning together
    public static class DocumentResult {

        private Document document;
        private String duplicateWarning;

        public DocumentResult(Document document, String duplicateWarning) {
            this.document = document;
            this.duplicateWarning = duplicateWarning;
        }

        public Document getDocument() { return document; }
        public String getDuplicateWarning() { return duplicateWarning; }
    }
}