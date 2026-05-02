package com.docusense.service; // This file belongs to the service package

import java.util.ArrayList; // Import Document model
import java.util.Comparator; // Import DocumentRepository
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Dynamic list

import com.docusense.model.Document; // Used to sort results
import com.docusense.model.DocumentRepository; // List of items

@Service // Tells Spring this is a service class
public class SearchService {

    @Autowired
    private DocumentRepository documentRepository; // Handles all database operations

    @Autowired
    private EmbeddingService embeddingService; // Generates embeddings for search query

    // Returns top 5 most relevant documents for a search query
    public List<SearchResult> search(String query) {

        // Step 1: Convert search query to embedding
        // e.g. "community organization" → [0.123, -0.456, ...]
        String queryEmbeddingStr = embeddingService.getEmbedding(query);

        // If embedding failed return empty list
        if (queryEmbeddingStr == null) return new ArrayList<>();

        // Step 2: Convert query embedding string to double array
        double[] queryEmbedding = parseEmbedding(queryEmbeddingStr);

        // Step 3: Get all documents from database
        List<Document> allDocuments = documentRepository.findAll();

        // Step 4: Create list to store search results with scores
        List<SearchResult> results = new ArrayList<>();

        // Step 5: Loop through each document and calculate similarity
        for (Document doc : allDocuments) {

            // Skip documents without embeddings
            if (doc.getEmbedding() == null) continue;

            // Convert document embedding string to double array
            double[] docEmbedding = parseEmbedding(doc.getEmbedding());

            // Calculate cosine similarity between query and document
            double similarity = cosineSimilarity(queryEmbedding, docEmbedding);

            // Add document and its similarity score to results
            results.add(new SearchResult(doc, similarity));
        }

        // Step 6: Sort results by similarity score (highest first)
        results.sort(Comparator.comparingDouble(SearchResult::getSimilarity).reversed());

        // Step 7: Return only top 5 results
        return results.subList(0, Math.min(5, results.size()));
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

        // Avoid division by zero
        if (magnitudeA == 0 || magnitudeB == 0) return 0;

        return dotProduct / (magnitudeA * magnitudeB);
    }

    // Inner class to hold document and its similarity score together
    public static class SearchResult {

        private Document document; // The matched document
        private double similarity; // How similar it is to the query (0 to 1)

        public SearchResult(Document document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }

        public Document getDocument() { return document; }
        public double getSimilarity() { return similarity; }
    }
}