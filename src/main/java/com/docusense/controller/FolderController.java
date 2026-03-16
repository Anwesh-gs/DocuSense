package com.docusense.controller;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.docusense.utils.FolderScanner;

@RestController
@RequestMapping("/api")
public class FolderController {

    private final FolderScanner folderScanner = new FolderScanner();

    // GET /api/scan-folder?path=C:\Users\anush\Desktop
    @GetMapping("/scan-folder")
    public List<String> scanFolder(@RequestParam String path) {

        System.out.println("Received scan request for: " + path);

        List<File> pdfFiles = folderScanner.scanForPdfs(path);

        // Convert File objects to just their names
        return pdfFiles.stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
