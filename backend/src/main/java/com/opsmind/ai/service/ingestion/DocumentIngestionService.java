package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.model.dto.DocumentResponseDto;
import com.opsmind.ai.model.dto.PdfPageContent;
import com.opsmind.ai.model.dto.TextChunk;
import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import com.opsmind.ai.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final PdfParserService pdfParserService;
    private final TextChunkerService textChunkerService;
    private final DocumentRepository documentRepository;
    private final SampleSopGeneratorService sampleSopGeneratorService;

    public DocumentIngestionService(
            PdfParserService pdfParserService,
            TextChunkerService textChunkerService,
            DocumentRepository documentRepository,
            SampleSopGeneratorService sampleSopGeneratorService) {
        this.pdfParserService = pdfParserService;
        this.textChunkerService = textChunkerService;
        this.documentRepository = documentRepository;
        this.sampleSopGeneratorService = sampleSopGeneratorService;
    }

    @Transactional
    public DocumentResponseDto ingestMultipartFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "document_" + System.currentTimeMillis() + ".pdf";
        }

        byte[] bytes = file.getBytes();
        DocumentEntity entity = ingestPdfBytes(filename, bytes, file.getContentType());
        return new DocumentResponseDto(entity, true);
    }

    @Transactional
    public DocumentEntity ingestPdfBytes(String filename, byte[] pdfBytes, String contentType) throws IOException {
        log.info("Starting ingestion for PDF document: {} ({} bytes)", filename, pdfBytes.length);

        // Delete existing document with the same name if re-uploading
        Optional<DocumentEntity> existing = documentRepository.findByFilename(filename);
        if (existing.isPresent()) {
            log.info("Document with filename '{}' already exists. Removing older version before re-ingestion.", filename);
            documentRepository.delete(existing.get());
            documentRepository.flush();
        }

        // 1. Extract physical pages using Apache PDFBox
        List<PdfPageContent> pages = pdfParserService.extractPages(pdfBytes);
        int totalPages = pages.size();

        // 2. Perform sliding-window text chunking with overlap
        List<TextChunk> textChunks = textChunkerService.chunkDocument(pages);
        log.info("Document '{}' parsed into {} pages and split into {} chunks with overlap",
                filename, totalPages, textChunks.size());

        // 3. Construct DocumentEntity
        String mimeType = (contentType != null && !contentType.isBlank()) ? contentType : "application/pdf";
        DocumentEntity document = new DocumentEntity(filename, mimeType, (long) pdfBytes.length, totalPages);
        document.setTotalChunks(textChunks.size());
        document.setStatus("INDEXED");

        // Generate brief initial summary from page 1 or initial chunks
        if (!textChunks.isEmpty()) {
            String firstSnippet = textChunks.get(0).getContent();
            String summary = firstSnippet.length() > 200 ? firstSnippet.substring(0, 197) + "..." : firstSnippet;
            document.setSummary(summary);
        }

        // 4. Attach chunks with page mapping & character offset metadata
        for (TextChunk tc : textChunks) {
            DocumentChunkEntity chunkEntity = new DocumentChunkEntity(
                    tc.getPageNumber(),
                    tc.getChunkIndex(),
                    tc.getContent(),
                    tc.getStartChar(),
                    tc.getEndChar()
            );
            document.addChunk(chunkEntity);
        }

        // 5. Transactional Hibernate persist (cascades to all chunks)
        DocumentEntity saved = documentRepository.save(document);
        log.info("Successfully persisted DocumentEntity ID={} with {} chunks via Hibernate", saved.getId(), saved.getTotalChunks());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDto> getAllDocuments() {
        return documentRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(doc -> new DocumentResponseDto(doc, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<DocumentResponseDto> getDocumentById(UUID id) {
        return documentRepository.findById(id)
                .map(doc -> new DocumentResponseDto(doc, true));
    }

    @Transactional
    public boolean deleteDocument(UUID id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
            log.info("Deleted document ID={} and cascade-deleted all associated chunks", id);
            return true;
        }
        return false;
    }

    @Transactional
    public synchronized List<DocumentEntity> seedSampleSops() {
        List<SampleSopGeneratorService.GeneratedPdf> samplePdfs = sampleSopGeneratorService.generateAllSampleSops();
        List<DocumentEntity> ingested = new java.util.ArrayList<>();
        for (SampleSopGeneratorService.GeneratedPdf pdf : samplePdfs) {
            try {
                DocumentEntity entity = ingestPdfBytes(pdf.filename(), pdf.bytes(), "application/pdf");
                ingested.add(entity);
                log.info("Successfully ingested sample SOP: {}", pdf.filename());
            } catch (Exception e) {
                log.error("Failed to seed sample SOP: {}", pdf.filename(), e);
            }
        }
        log.info("Completed seeding {} enterprise SOPs into OpsMind AI Neural Brain", ingested.size());
        return ingested;
    }
}
