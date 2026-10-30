package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.model.dto.PdfPageContent;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserService.class);

    public List<PdfPageContent> extractPages(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            return extractPagesFromDocument(document);
        }
    }

    public List<PdfPageContent> extractPages(InputStream inputStream) throws IOException {
        try (RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputStream);
             PDDocument document = Loader.loadPDF(buffer)) {
            return extractPagesFromDocument(document);
        }
    }

    private List<PdfPageContent> extractPagesFromDocument(PDDocument document) throws IOException {
        int totalPages = document.getNumberOfPages();
        List<PdfPageContent> pages = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();

        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            stripper.setStartPage(pageNum);
            stripper.setEndPage(pageNum);
            String pageText = stripper.getText(document);
            if (pageText != null) {
                pageText = pageText.trim();
            } else {
                pageText = "";
            }
            pages.add(new PdfPageContent(pageNum, pageText));
        }

        log.info("Extracted {} pages from PDF document", totalPages);
        return pages;
    }
}
