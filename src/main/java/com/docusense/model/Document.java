package com.docusense.model;

import java.time.LocalDateTime; // Import this

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;

    @JsonIgnore // Don't send extracted text in response - too large
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    private String category;

    @JsonIgnore // Don't send embedding in response - too large
    @Column(columnDefinition = "LONGTEXT")
    private String embedding;

    private String filePath;

    private LocalDateTime uploadedAt;

    @PrePersist
    public void prePersist() {
        uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    private boolean isDuplicate; // True if this document is a duplicate of another

// Add getter and setter at the bottom
public boolean isDuplicate() { return isDuplicate; }
public void setDuplicate(boolean isDuplicate) { this.isDuplicate = isDuplicate; }
}