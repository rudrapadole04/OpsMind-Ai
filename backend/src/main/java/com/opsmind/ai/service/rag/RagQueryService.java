package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.model.dto.QueryResponse;
import com.opsmind.ai.model.entity.CitationEntity;
import com.opsmind.ai.model.entity.QueryLogEntity;
import com.opsmind.ai.repository.QueryLogRepository;
import com.opsmind.ai.service.graph.KnowledgeGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagQueryService {

    private static final Logger log = LoggerFactory.getLogger(RagQueryService.class);

    private final VectorSearchService vectorSearchService;
    private final HallucinationGuardrailService guardrailService;
    private final QueryLogRepository queryLogRepository;
    private final KnowledgeGraphService knowledgeGraphService;

    public RagQueryService(
            VectorSearchService vectorSearchService,
            HallucinationGuardrailService guardrailService,
            QueryLogRepository queryLogRepository,
            KnowledgeGraphService knowledgeGraphService) {
        this.vectorSearchService = vectorSearchService;
        this.guardrailService = guardrailService;
        this.queryLogRepository = queryLogRepository;
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @Transactional
    public QueryResponse processQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        String question = request.getQuestion().trim();
        log.info("Processing employee query: '{}'", question);

        // 1. Semantic Vector Retrieval
        List<ScoredChunk> retrievedChunks = vectorSearchService.search(question);

        // 2. Hallucination Guardrail Validation & Answer Synthesis
        HallucinationGuardrailService.GuardrailEvaluation evaluation =
                guardrailService.evaluateAndSynthesize(question, retrievedChunks);

        long latencyMs = System.currentTimeMillis() - startTime;

        // 3. Persist Query Log and Citations via Hibernate
        QueryLogEntity logEntity = new QueryLogEntity(
                question,
                evaluation.synthesizedAnswer(),
                evaluation.confidenceScore(),
                evaluation.guardrailTriggered(),
                latencyMs
        );

        if (!evaluation.guardrailTriggered() && evaluation.citations() != null) {
            for (CitationDto c : evaluation.citations()) {
                CitationEntity citation = new CitationEntity(
                        c.getSourceFilename(),
                        c.getPageNumber(),
                        c.getExcerpt(),
                        c.getRelevanceScore()
                );
                logEntity.addCitation(citation);
            }
        }

        QueryLogEntity saved = queryLogRepository.save(logEntity);
        log.info("Query logged ID={} (Guardrail Triggered={}, Latency={}ms)",
                saved.getId(), evaluation.guardrailTriggered(), latencyMs);

        // 4. Increment topic node frequencies and edge weights in Knowledge Graph
        try {
            knowledgeGraphService.recordQueryTopicHits(question, saved.getCitations());
        } catch (Exception e) {
            log.warn("Failed to record query topic hits in Knowledge Graph: {}", e.getMessage());
        }

        return new QueryResponse(
                question,
                evaluation.synthesizedAnswer(),
                evaluation.confidenceScore(),
                evaluation.guardrailTriggered(),
                latencyMs,
                evaluation.citations()
        );
    }

    @Transactional(readOnly = true)
    public List<QueryResponse> getQueryHistory() {
        return queryLogRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(QueryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QueryResponse> getKnowledgeGaps() {
        return queryLogRepository.findByGuardrailTriggeredTrueOrderByCreatedAtDesc().stream()
                .map(QueryResponse::new)
                .collect(Collectors.toList());
    }
}
