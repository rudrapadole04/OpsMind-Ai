package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HallucinationGuardrailService {

    private static final Logger log = LoggerFactory.getLogger(HallucinationGuardrailService.class);

    private final double similarityThreshold;
    private final String fallbackMessage;
    private final PrecisionCitationEngine citationEngine;

    public record GuardrailEvaluation(
            boolean guardrailTriggered,
            double confidenceScore,
            String synthesizedAnswer,
            List<CitationDto> citations
    ) {}

    public HallucinationGuardrailService(
            @Value("${opsmind.rag.similarity-threshold:0.60}") double similarityThreshold,
            @Value("${opsmind.rag.fallback-message:I don't know based on the provided corporate SOPs.}") String fallbackMessage,
            PrecisionCitationEngine citationEngine) {
        this.similarityThreshold = similarityThreshold;
        this.fallbackMessage = fallbackMessage;
        this.citationEngine = citationEngine;
    }

    public GuardrailEvaluation evaluateAndSynthesize(String question, List<ScoredChunk> retrievedChunks) {
        if (retrievedChunks == null || retrievedChunks.isEmpty()) {
            log.info("Guardrail ACTIVATED for question '{}' (No chunks retrieved)", question);
            return new GuardrailEvaluation(true, 0.0, fallbackMessage, Collections.emptyList());
        }

        double topScore = retrievedChunks.get(0).getScore();

        // 1. Threshold Guardrail Check
        if (topScore < similarityThreshold) {
            log.info("Guardrail ACTIVATED for question '{}': Top score ({}) below threshold ({})",
                    question, String.format("%.2f", topScore), similarityThreshold);
            return new GuardrailEvaluation(true, topScore, fallbackMessage, Collections.emptyList());
        }

        // 2. Build verified precision citations
        List<CitationDto> citations = citationEngine.buildCitations(retrievedChunks);

        // 3. Grounded answer synthesis
        StringBuilder synthesis = new StringBuilder();
        synthesis.append("Based on verified corporate SOP documentation:\n\n");

        for (int i = 0; i < retrievedChunks.size(); i++) {
            ScoredChunk sc = retrievedChunks.get(i);
            if (sc.getScore() >= similarityThreshold) {
                String sourceDoc = sc.getChunk().getDocument().getFilename();
                int page = sc.getChunk().getPageNumber();
                synthesis.append(String.format("• %s [%s, Page %d]\n",
                        sc.getMostRelevantSnippet(), sourceDoc, page));
            }
        }

        String finalAnswer = citationEngine.generateCitedAnswer(synthesis.toString(), citations);
        log.info("Guardrail PASSED for question '{}': Confidence score={}", question, String.format("%.2f", topScore));

        return new GuardrailEvaluation(false, topScore, finalAnswer, citations);
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }
}
