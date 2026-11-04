package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.repository.DocumentRepository;
import com.opsmind.ai.service.graph.KnowledgeGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "opsmind.seed.enabled", havingValue = "true", matchIfMissing = true)
public class SampleSopInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleSopInitializer.class);

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;
    private final KnowledgeGraphService knowledgeGraphService;

    public SampleSopInitializer(
            DocumentRepository documentRepository,
            DocumentIngestionService ingestionService,
            KnowledgeGraphService knowledgeGraphService) {
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @Override
    public void run(ApplicationArguments args) {
        long existingDocs = documentRepository.countTotalDocuments();
        if (existingDocs == 0) {
            log.info("No documents found in knowledge base. Seeding realistic enterprise corporate SOPs...");
            ingestionService.seedSampleSops();
        } else {
            log.info("Knowledge base already contains {} documents. Skipping automatic sample seeding.", existingDocs);
        }

        // Initialize/sync Knowledge Graph topology on startup
        try {
            knowledgeGraphService.buildOrSyncGraph();
            log.info("OpsMind Knowledge Graph topology successfully synchronized on boot.");
        } catch (Exception e) {
            log.warn("Knowledge Graph auto-sync deferred: {}", e.getMessage());
        }
    }
}
