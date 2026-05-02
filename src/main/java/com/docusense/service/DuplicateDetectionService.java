package com.docusense.service; // This file belongs to the service package

import java.util.List; // Import Document model

import org.springframework.beans.factory.annotation.Autowired; // Import DocumentRepository
import org.springframework.stereotype.Service;

import com.docusense.model.Document;
import com.docusense.model.DocumentRepository;

@Service // Tells Spring this is a service class
public class DuplicateDetectionService {

    @Autowired
    private DocumentRepository documentRepository; // Handles all database operations

    // Threshold for duplicate detection
    // If similarity is above this value → document is a duplicate
    private static final double DUPLICATE_THRESHOLD = 0.95;

    public String checkDuplicate(String newEmbeddingStr, Long currentDocumentId) {

        // Step 1: Get all existing documents from database
        List<Document> allDocuments = documentRepository.findAll();

        // Step 2: Loop through each existing document
        for (Document doc : allDocuments) {

            // Skip documents that don't have embeddings
            if (doc.getEmbedding() == null) continue;

            // Skip the current document itself
            // We don't want to compare a document with itself
            if (doc.getId().equals(currentDocumentId)) continue;

            // Step 3: Convert both embeddings to double arrays
            double[] newEmbedding = parseEmbedding(newEmbeddingStr);
            double[] existingEmbedding = parseEmbedding(doc.getEmbedding());

            // Step 4: Calculate cosine similarity between the two documents
            double similarity = cosineSimilarity(newEmbedding, existingEmbedding);

            // Step 5: If similarity is above threshold → duplicate found!
            if (similarity >= DUPLICATE_THRESHOLD) {
                return "⚠️ Duplicate detected! Similar to: " + doc.getFilename()
                        + " (similarity: " + String.format("%.2f", similarity) + ")";
            }
        }

        // No duplicate found
        return null;
    }

    // Converts embedding string "[0.123, -0.456, ...]" to double array
    private double[] parseEmbedding(String embeddingStr) {

        // Remove square brackets
        embeddingStr = embeddingStr.replace("[", "").replace("]", "");

        // Split by comma to get individual numbers
        String[] parts = embeddingStr.split(",");

        // Create double array
        double[] embedding = new double[parts.length];

        // Convert each string to double
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i].trim());
        }

        return embedding;
    }

    // Calculates cosine similarity between two vectors
    // Same formula as ClassificationService
    private double cosineSimilarity(double[] vectorA, double[] vectorB) {

        double dotProduct = 0.0; // Sum of products of matching elements
        double magnitudeA = 0.0; // Sum of squares of A
        double magnitudeB = 0.0; // Sum of squares of B

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += vectorA[i] * vectorA[i];
            magnitudeB += vectorB[i] * vectorB[i];
        }

        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        if (magnitudeA == 0 || magnitudeB == 0) return 0;

        return dotProduct / (magnitudeA * magnitudeB);
    }
}