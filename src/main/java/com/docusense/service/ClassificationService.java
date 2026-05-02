package com.docusense.service; // This file belongs to the service package

import java.util.List; // Import Category model

import org.springframework.beans.factory.annotation.Autowired; // Import CategoryRepository
import org.springframework.stereotype.Service;

import com.docusense.model.Category;
import com.docusense.model.CategoryRepository;

@Service // Tells Spring this is a service class
public class ClassificationService {

    @Autowired
    private CategoryRepository categoryRepository; // Spring injects repository automatically

    @Autowired
    private EmbeddingService embeddingService; // Used to get embedding for the PDF text

    public String classify(String text) {

        // Step 1: Get the AI embedding for the PDF text
        // This converts the PDF text into 384 numbers representing its meaning
        String textEmbeddingStr = embeddingService.getEmbedding(text);

        // If embedding failed return Unknown
        if (textEmbeddingStr == null) return "Unknown";

        // Step 2: Convert the embedding string to a double array
        double[] textEmbedding = parseEmbedding(textEmbeddingStr);

        // Step 3: Get all categories from database
        List<Category> categories = categoryRepository.findAll();

        // Variables to track best matching category
        String bestCategory = "Unknown";
        double highestSimilarity = -1; // Cosine similarity ranges from -1 to 1

        // Step 4: Loop through each category and calculate similarity
        for (Category category : categories) {

            // Skip categories that don't have embeddings yet
            if (category.getEmbedding() == null) continue;

            // Convert category embedding string to double array
            double[] categoryEmbedding = parseEmbedding(category.getEmbedding());

            // Calculate cosine similarity between PDF and category
            double similarity = cosineSimilarity(textEmbedding, categoryEmbedding);

            // If this category is more similar than previous best → update
            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestCategory = category.getName();
            }
        }

        System.out.println("Best category: " + bestCategory + " with similarity: " + highestSimilarity);

        return bestCategory;
    }

    // Converts embedding string "[0.123, -0.456, ...]" to double array [0.123, -0.456, ...]
    private double[] parseEmbedding(String embeddingStr) {

        // Remove the square brackets [ and ] from the string
        embeddingStr = embeddingStr.replace("[", "").replace("]", "");

        // Split by comma to get individual numbers
        String[] parts = embeddingStr.split(",");

        // Create a double array of the same size
        double[] embedding = new double[parts.length];

        // Convert each string number to a double
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i].trim());
        }

        return embedding;
    }

    // Calculates cosine similarity between two vectors
    // Cosine similarity = (A · B) / (|A| × |B|)
    private double cosineSimilarity(double[] vectorA, double[] vectorB) {

        double dotProduct = 0.0; // A · B → multiply matching elements and sum
        double magnitudeA = 0.0; // |A| → square root of sum of squares
        double magnitudeB = 0.0; // |B| → square root of sum of squares

        // Loop through each element and calculate
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i]; // Multiply matching elements
            magnitudeA += vectorA[i] * vectorA[i]; // Square each element of A
            magnitudeB += vectorB[i] * vectorB[i]; // Square each element of B
        }

        // Square root to get the magnitude
        magnitudeA = Math.sqrt(magnitudeA);
        magnitudeB = Math.sqrt(magnitudeB);

        // Avoid division by zero
        if (magnitudeA == 0 || magnitudeB == 0) return 0;

        // Return the cosine similarity score
        return dotProduct / (magnitudeA * magnitudeB);
    }
}