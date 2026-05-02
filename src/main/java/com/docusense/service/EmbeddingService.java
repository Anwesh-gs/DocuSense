package com.docusense.service; // This file belongs to the service package

import java.util.List;
import java.util.Map; // Used to make HTTP calls to other services

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service // Tells Spring this is a service class
public class EmbeddingService {

    // RestTemplate is Spring's built-in tool for making HTTP requests
    // We use it to call our Python FastAPI service
    private final RestTemplate restTemplate = new RestTemplate();

    // URL of our Python FastAPI embedding service
    private final String PYTHON_API_URL = "http://localhost:8000/embed";

    public String getEmbedding(String text) {
        try {
            // Step 1: Set the request headers to tell the API we're sending JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // Content type is JSON

            // Step 2: Create the request body with the text
            // This creates: {"text": "your text here"}
            Map<String, String> requestBody = Map.of("text", text);

            // Step 3: Combine headers and body into one request object
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Step 4: Send POST request to Python service and get response
            // Map.class tells RestTemplate to parse the JSON response into a Map
            ResponseEntity<Map> response = restTemplate.postForEntity(
                PYTHON_API_URL, // URL to call
                request,        // Request body and headers
                Map.class        // Expected response type
            );

            // Step 5: Extract the embedding list from the response
            // Response looks like: {"embedding": [0.123, -0.456, ...]}
            List<Double> embedding = (List<Double>) response.getBody().get("embedding");

            // Step 6: Convert the list to a JSON string for database storage
            // e.g. [0.123, -0.456, 0.789, ...]
            return embedding.toString();

        } catch (Exception e) {
            // If Python service is down or fails, log the error and return null
            System.out.println("Error getting embedding: " + e.getMessage());
            return null;
        }
    }
}