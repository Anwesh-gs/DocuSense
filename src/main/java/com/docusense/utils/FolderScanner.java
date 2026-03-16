package com.docusense.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderScanner {

    // Takes a folder path and returns a list of PDF files found inside
    public List<File> scanForPdfs(String folderPath) {

        List<File> pdfFiles = new ArrayList<>();

        // Step 1: Point to the folder
        File folder = new File(folderPath);

        // Step 2: Check the folder actually exists
        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path: " + folderPath);
            return pdfFiles;
        }

        // Step 3: List all files in the folder
        File[] allFiles = folder.listFiles();

        if (allFiles == null) {
            System.out.println("Folder is empty.");
            return pdfFiles;
        }

        // Step 4: Filter only .pdf files
        System.out.println("Scanning folder: " + folderPath);
        System.out.println("----------------------------");

        for (File file : allFiles) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                pdfFiles.add(file);
                System.out.println("Found PDF: " + file.getName());
            }
        }

        // Step 5: Summary
        System.out.println("----------------------------");
        System.out.println("Total PDFs found: " + pdfFiles.size());

        return pdfFiles;
    }
public static void main(String[] args) {
    FolderScanner scanner = new FolderScanner();
    scanner.scanForPdfs("C:\\Users\\anush\\OneDrive\\Desktop");
}
}
