package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.model.dto.DocumentResponseDto;
import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import com.opsmind.ai.repository.DocumentChunkRepository;
import com.opsmind.ai.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentIngestionIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private SampleSopGeneratorService generatorService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Test
    @DisplayName("Should ingest PDF Multipart file, extract pages, chunk with overlap, and persist to Hibernate")
    void shouldIngestMultipartFileAndPersist() throws IOException {
        SampleSopGeneratorService.GeneratedPdf samplePdf = generatorService.generateSickLeaveSop();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                samplePdf.filename(),
                "application/pdf",
                samplePdf.bytes()
        );

        DocumentResponseDto response = ingestionService.ingestMultipartFile(multipartFile);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFilename()).isEqualTo("HR_Extended_Sick_Leave_Policy.pdf");
        assertThat(response.getTotalPages()).isEqualTo(3);
        assertThat(response.getTotalChunks()).isGreaterThanOrEqualTo(3);
        assertThat(response.getStatus()).isEqualTo("INDEXED");

        // Verify database persistence
        Optional<DocumentEntity> docOpt = documentRepository.findById(response.getId());
        assertThat(docOpt).isPresent();

        List<DocumentChunkEntity> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(response.getId());
        assertThat(chunks).hasSize(response.getTotalChunks());

        // Check that page 1, 2, and 3 are present across chunks
        boolean hasPage1 = chunks.stream().anyMatch(c -> c.getPageNumber() == 1);
        boolean hasPage2 = chunks.stream().anyMatch(c -> c.getPageNumber() == 2);
        boolean hasPage3 = chunks.stream().anyMatch(c -> c.getPageNumber() == 3);

        assertThat(hasPage1).isTrue();
        assertThat(hasPage2).isTrue();
        assertThat(hasPage3).isTrue();
    }

    @Test
    @DisplayName("Should cascade delete chunks when document is deleted")
    void shouldCascadeDeleteChunks() throws IOException {
        SampleSopGeneratorService.GeneratedPdf samplePdf = generatorService.generateItVpnSop();
        DocumentEntity entity = ingestionService.ingestPdfBytes(samplePdf.filename(), samplePdf.bytes(), "application/pdf");

        assertThat(documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(entity.getId())).isNotEmpty();

        boolean deleted = ingestionService.deleteDocument(entity.getId());
        assertThat(deleted).isTrue();

        assertThat(documentRepository.findById(entity.getId())).isEmpty();
        assertThat(documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(entity.getId())).isEmpty();
    }
}
