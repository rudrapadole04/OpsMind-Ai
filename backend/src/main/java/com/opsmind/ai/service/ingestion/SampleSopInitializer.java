package com.opsmind.ai.service.ingestion;

import com.opsmind.ai.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

//@Component
//@ConditionalOnProperty(name = "opsmind.seed.enabled", havingValue = "true", matchIfMissing = false)
public class SampleSopInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleSopInitializer.class);

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;

    public SampleSopInitializer(
            DocumentRepository documentRepository,
            DocumentIngestionService ingestionService) {
        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
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
    }
}
