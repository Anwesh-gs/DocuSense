package com.docusense.service;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PdfExtractorService {

    @Autowired
    private TextCleanerService textCleanerService;

    public String extractText(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        
        PDFTextStripper stripper = new PDFTextStripper();
        
        // Handle multi-page PDFs - extract all pages
        stripper.setStartPage(1);
        stripper.setEndPage(document.getNumberOfPages());
        
        String rawText = stripper.getText(document);
        document.close();

        // Clean the extracted text
        String cleanedText = textCleanerService.cleanText(rawText);
        
        return cleanedText;
    }
}