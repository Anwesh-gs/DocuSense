package com.docusense.controller; // This file belongs to the controller package

import java.io.File; // Import our Document model
import java.util.HashMap; // Import DocumentRepository
import java.util.LinkedHashMap; // Import our DocumentService
import java.util.List; // Import inner class
import java.util.Map; // Import SearchService
import java.util.stream.Collectors; // Import SearchResult inner class

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource; // Used to serve files
import org.springframework.core.io.Resource; // Resource interface
import org.springframework.http.HttpHeaders; // HTTP headers
import org.springframework.http.MediaType; // Media types
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.docusense.model.Document;
import com.docusense.model.DocumentRepository;
import com.docusense.service.DocumentService;
import com.docusense.service.DocumentService.DocumentResult;
import com.docusense.service.SearchService;
import com.docusense.service.SearchService.SearchResult;

@RestController // Tells Spring this class handles HTTP requests
@RequestMapping("/api") // All endpoints start with /api
@CrossOrigin(origins = "*") // Allows requests from any origin
public class PdfController {

    @Autowired
    private DocumentService documentService; // Spring injects DocumentService automatically

    @Autowired
    private SearchService searchService; // Spring injects SearchService automatically

    @Autowired
    private DocumentRepository documentRepository; // Access documents directly

    // POST /api/upload → handles single PDF upload
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file) {
        try {
            DocumentResult result = documentService.saveDocument(file);

            Map<String, Object> response = new HashMap<>();
            response.put("document", result.getDocument());
            response.put("duplicateWarning", result.getDuplicateWarning());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

   // POST /api/upload-bulk → handles multiple PDF uploads
@PostMapping("/upload-bulk")
public ResponseEntity<List<Map<String, Object>>> uploadMultiplePdfs(
        @RequestParam("files") List<MultipartFile> files) {
    try {
        List<DocumentResult> results = documentService.saveAllDocuments(files);

        List<Map<String, Object>> response = results.stream()
            .map(result -> {
                Map<String, Object> map = new HashMap<>();
                map.put("document", result.getDocument());
                map.put("duplicateWarning", result.getDuplicateWarning());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        // Print full error to terminal
        e.printStackTrace();
        System.out.println("UPLOAD ERROR: " + e.getMessage());
        return ResponseEntity.internalServerError().build();
    }
}

    // GET /api/documents/grouped → returns all documents grouped by category
    // This is called every time the app loads to show the folder structure
    @GetMapping("/documents/grouped")
    public ResponseEntity<Map<String, List<Document>>> getGroupedDocuments() {
        try {
            // Get all documents from database
            List<Document> allDocuments = documentRepository.findAll();

            // Group documents by category
            // LinkedHashMap maintains insertion order
            Map<String, List<Document>> grouped = allDocuments.stream()
                .collect(Collectors.groupingBy(
                    doc -> doc.getCategory() != null ? doc.getCategory() : "Unknown",
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

            return ResponseEntity.ok(grouped);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/documents/{id}/view → serves the actual PDF file for viewing
    @GetMapping("/documents/{id}/view")
    public ResponseEntity<Resource> viewDocument(@PathVariable Long id) {
        try {
            // Find document in database
            Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

            // Get the file from disk using stored path
            File file = new File(document.getFilePath());

            // Check if file exists
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Create resource from file
            Resource resource = new FileSystemResource(file);

            // Return file with PDF content type
            // inline means browser will display it instead of downloading
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + document.getFilename() + "\"")
                .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/search?query=... → semantic search across all PDFs
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam("query") String query) {
        try {
            List<SearchResult> results = searchService.search(query);

            List<Map<String, Object>> response = results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("document", result.getDocument());
                    map.put("similarity", result.getSimilarity());
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    // DELETE /api/documents/{id} → deletes a document
@DeleteMapping("/documents/{id}")
public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
    try {
        // Find document in database
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Delete the actual file from disk
        File file = new File(document.getFilePath());
        if (file.exists()) {
            file.delete(); // Delete file from server
        }

        // Delete from database
        documentRepository.deleteById(id);

        return ResponseEntity.ok("Document deleted successfully");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
    }
}

// PUT /api/documents/{id}/move → moves document to a new category
@PutMapping("/documents/{id}/move")
public ResponseEntity<Document> moveDocument(
        @PathVariable Long id,
        @RequestParam("category") String newCategory) { // New category from request
    try {
        // Find document in database
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found"));

        // Update the category
        document.setCategory(newCategory);

        // Save updated document
        Document updated = documentRepository.save(document);

        return ResponseEntity.ok(updated);

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }
}
}