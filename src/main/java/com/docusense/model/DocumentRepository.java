package com.docusense.model; // This file belongs to the model package

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Tells Spring this is a repository class that talks to the database
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // JpaRepository gives us free database methods like:
    // save() → insert or update a document
    // findById() → find a document by its ID
    // findAll() → get all documents
    // deleteById() → delete a document by its ID
    // No need to write SQL queries manually!
}