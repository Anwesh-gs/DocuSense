package com.docusense.model; // This file belongs to the model package

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // Tells JPA this class maps to a database table
@Table(name = "categories") // Table will be called "categories" in MySQL
public class Category {

    @Id // This field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto increments ID (1, 2, 3...)
    private Long id;

    private String name; // Category name e.g. "OS", "DBMS", "Networks", "AI"

    @Column(columnDefinition = "TEXT") // Use TEXT type because keywords list can be long
    private String keywords; // Comma separated keywords e.g. "process,thread,memory,kernel"

    @Column(columnDefinition = "LONGTEXT") // Embedding is a long list of numbers
    private String embedding; // AI embedding of the category keywords stored as JSON string

    // Getters and Setters - used to get and set each field's value
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getEmbedding() { return embedding; }
    public void setEmbedding(String embedding) { this.embedding = embedding; }
}