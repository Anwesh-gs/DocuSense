package com.docusense.service; // This file belongs to the service package

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // Used to make HTTP calls
import org.springframework.http.*;
import java.util.Map;

@Service // Tells Spring this is a service class
public class DynamicClassificationService {

    // RestTemplate makes HTTP calls to our Python service
    private final RestTemplate restTemplate = new RestTemplate();

    // URL of our Python classify endpoint
    private final String PYTHON_CLASSIFY_URL = "http://localhost:8000/classify";

    public String classify(String text) {
        try {
            // Step 1: Set request headers to JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Step 2: Create request body with the text
            // Sends: {"text": "your document text here"}
            Map<String, String> requestBody = Map.of("text", text);

            // Step 3: Combine headers and body
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Step 4: Call Python classify endpoint
            ResponseEntity<Map> response = restTemplate.postForEntity(
                PYTHON_CLASSIFY_URL, // URL to call
                request,             // Request body and headers
                Map.class            // Expected response type
            );

            // Step 5: Extract category from response
            // Response looks like: {"category": "Community Service Programs"}
            String category = (String) response.getBody().get("category");

            System.out.println("Dynamic category generated: " + category);

            return category;

        } catch (Exception e) {
            // If Python service is down return Unknown
            System.out.println("Error classifying: " + e.getMessage());
            return "Unknown";
        }
    }
}