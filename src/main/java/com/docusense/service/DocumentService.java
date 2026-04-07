package com.docusense.service; // This file belongs to the service package

import java.util.ArrayList; // Import our Document model
import java.util.List; // Import our repository

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.docusense.model.Document; // Allows us to create a dynamic list
import com.docusense.model.DocumentRepository; // Represents a list of items

@Service // Tells Spring this is a service class
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository; // Handles all database operations

    @Autowired
    private PdfExtractorService pdfExtractorService; // Extracts text from PDFs

    // Saves a single PDF to the database
    public Document saveDocument(MultipartFile file) throws Exception {

        // Step 1: Extract and clean text from the uploaded PDF
        String cleanedText = pdfExtractorService.extractText(file);

        // Step 2: Create a new Document object
        Document document = new Document();

        // Step 3: Set the filename from the uploaded file
        document.setFilename(file.getOriginalFilename());

        // Step 4: Set the extracted and cleaned text
        document.setExtractedText(cleanedText);

        // Step 5: Save to database and return
        return documentRepository.save(document);
    }

    // Saves multiple PDFs to the database one by one
    public List<Document> saveAllDocuments(List<MultipartFile> files) throws Exception {

        // Create an empty list to store all saved documents
        List<Document> savedDocuments = new ArrayList<>();

        // Loop through each uploaded file one by one
        for (MultipartFile file : files) {

            // Skip empty files to avoid errors
            if (!file.isEmpty()) {

                // Save each file using our existing saveDocument method
                Document saved = saveDocument(file);

                // Add the saved document to our list
                savedDocuments.add(saved);
            }
        }

        // Return the list of all saved documents
        return savedDocuments;
    }
}