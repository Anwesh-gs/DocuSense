package com.docusense.model; // This file belongs to the model package

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Tells Spring this is a database layer class
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // JpaRepository gives us free methods:
    // save() → insert or update a category
    // findAll() → get all categories
    // findById() → find category by ID
    // deleteById() → delete category by ID

    // Custom method - Spring automatically generates the SQL for this!
    // Finds a category by its name e.g. findByName("OS")
    Category findByName(String name);
}