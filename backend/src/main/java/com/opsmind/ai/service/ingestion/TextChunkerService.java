package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.model.dto.PdfPageContent;
import com.opsmind.ai.model.dto.TextChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkerService {

    private final int chunkSize;
    private final int chunkOverlap;

    public TextChunkerService(
            @Value("${opsmind.rag.chunk-size:500}") int chunkSize,
            @Value("${opsmind.rag.chunk-overlap:100}") int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<TextChunk> chunkDocument(List<PdfPageContent> pages) {
        List<TextChunk> allChunks = new ArrayList<>();
        int globalChunkIndex = 0;

        for (PdfPageContent page : pages) {
            String text = page.getContent();
            if (text == null || text.isBlank()) {
                continue;
            }

            List<TextChunk> pageChunks = chunkPageText(page.getPageNumber(), text, globalChunkIndex);
            allChunks.addAll(pageChunks);
            globalChunkIndex += pageChunks.size();
        }

        return allChunks;
    }

    public List<TextChunk> chunkPageText(int pageNumber, String text, int startGlobalIndex) {
        List<TextChunk> chunks = new ArrayList<>();
        int length = text.length();

        if (length <= chunkSize) {
            chunks.add(new TextChunk(pageNumber, startGlobalIndex, text.trim(), 0, length));
            return chunks;
        }

        int step = Math.max(1, chunkSize - chunkOverlap);
        int start = 0;
        int chunkIdx = startGlobalIndex;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            // If not at the very end of text, attempt to find a clean sentence or word boundary
            if (end < length) {
                int adjustedEnd = findNaturalBreak(text, start, end);
                if (adjustedEnd > start) {
                    end = adjustedEnd;
                }
            }

            String chunkContent = text.substring(start, end).trim();
            if (!chunkContent.isEmpty()) {
                chunks.add(new TextChunk(pageNumber, chunkIdx++, chunkContent, start, end));
            }

            if (end >= length) {
                break;
            }

            start += step;
            if (start >= end) {
                start = end;
            }
        }

        return chunks;
    }

    private int findNaturalBreak(String text, int start, int preferredEnd) {
        int searchStart = Math.max(start + (chunkSize / 2), preferredEnd - 50);
        for (int i = preferredEnd; i >= searchStart; i--) {
            char c = text.charAt(i - 1);
            if (c == '.' || c == '\n' || c == '?' || c == '!') {
                return i;
            }
        }
        for (int i = preferredEnd; i >= Math.max(start + 1, preferredEnd - 30); i--) {
            if (Character.isWhitespace(text.charAt(i - 1))) {
                return i;
            }
        }
        return preferredEnd;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }
}
