package com.docusense.service;

import org.springframework.stereotype.Service;

@Service
public class TextCleanerService {

    public String cleanText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        String cleaned = rawText;

        // Fix hyphenated line breaks (impor-\ntant → important)
        cleaned = cleaned.replaceAll("-\\n", "");

        // Remove extra whitespace and blank lines
        cleaned = cleaned.replaceAll("\\s+", " ");

        // Remove special characters but keep letters, numbers, punctuation
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9\\s.,!?;:'\"-]", "");

        // Trim leading and trailing spaces
        cleaned = cleaned.trim();

        return cleaned;
    }
}