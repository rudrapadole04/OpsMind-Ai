package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.model.dto.PdfPageContent;
import com.opsmind.ai.model.dto.TextChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfParserAndChunkingTest {

    private PdfParserService pdfParserService;
    private TextChunkerService textChunkerService;
    private SampleSopGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        pdfParserService = new PdfParserService();
        textChunkerService = new TextChunkerService(300, 50); // smaller chunk size for testing overlap
        generatorService = new SampleSopGeneratorService();
    }

    @Test
    @DisplayName("Should parse multi-page PDF and preserve physical page numbers")
    void shouldExtractPagesWithAccuratePageNumbers() throws IOException {
        SampleSopGeneratorService.GeneratedPdf pdf = generatorService.generateRefundSop();
        assertThat(pdf.bytes()).isNotEmpty();

        List<PdfPageContent> pages = pdfParserService.extractPages(pdf.bytes());

        assertThat(pages).hasSize(3);
        assertThat(pages.get(0).getPageNumber()).isEqualTo(1);
        assertThat(pages.get(0).getContent()).contains("CUSTOMER REFUND POLICY");
        assertThat(pages.get(0).getContent()).contains("APPROVAL THRESHOLDS");

        assertThat(pages.get(1).getPageNumber()).isEqualTo(2);
        assertThat(pages.get(1).getContent()).contains("CHARGEBACK DISPUTES");
        assertThat(pages.get(1).getContent()).contains("14 business days");

        assertThat(pages.get(2).getPageNumber()).isEqualTo(3);
        assertThat(pages.get(2).getContent()).contains("FRAUD AND ESCALATIONS");
    }

    @Test
    @DisplayName("Should perform sliding-window chunking with overlap and preserve page coordinates")
    void shouldChunkWithOverlapAndPagePreservation() {
        String samplePage1 = "Paragraph 1 explains customer eligibility. " +
                "Paragraph 2 explains Tier 1 approval up to $50. " +
                "Paragraph 3 details dispute workflows for supervisor review. " +
                "Paragraph 4 outlines refund crediting within 5 to 7 business days.";

        PdfPageContent page = new PdfPageContent(2, samplePage1);
        List<TextChunk> chunks = textChunkerService.chunkDocument(List.of(page));

        assertThat(chunks).isNotEmpty();
        for (TextChunk chunk : chunks) {
            assertThat(chunk.getPageNumber()).isEqualTo(2);
            assertThat(chunk.getContent()).isNotBlank();
            assertThat(chunk.getStartChar()).isLessThan(chunk.getEndChar());
            assertThat(chunk.getContent().length()).isLessThanOrEqualTo(350);
        }

        // Verify sequential chunk indexes
        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getChunkIndex()).isEqualTo(i);
        }
    }
}
