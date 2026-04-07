package com.docusense.model; // This file belongs to the model package

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // Tells JPA this class maps to a database table
@Table(name = "documents") // The table will be called "documents" in MySQL
public class Document {

    @Id // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto increments ID (1, 2, 3...)
    private Long id;

    private String filename; // Stores the PDF filename

    @Column(columnDefinition = "LONGTEXT") // Use LONGTEXT because extracted text can be very large
    private String extractedText; // Stores the cleaned extracted text

    private LocalDateTime uploadedAt; // Stores when the PDF was uploaded

    @PrePersist // This method runs automatically before saving to database
    public void prePersist() {
        uploadedAt = LocalDateTime.now(); // Sets upload time to right now
    }

    // Getters and Setters - used to get and set each field's value

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}