package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

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
            @Value("${opsmind.rag.fallback-message:I am not able to answer this based on the provided corporate SOPs.}") String fallbackMessage,
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

        // 3. Conversational Bot Answer Synthesis
        String conversationalAnswer = synthesizeConversationalAnswer(question, retrievedChunks);
        String finalAnswer = citationEngine.generateCitedAnswer(conversationalAnswer, citations);
        log.info("Guardrail PASSED for question '{}': Confidence score={}", question, String.format("%.2f", topScore));

        return new GuardrailEvaluation(false, topScore, finalAnswer, citations);
    }

    private String synthesizeConversationalAnswer(String question, List<ScoredChunk> retrievedChunks) {
        if (retrievedChunks.isEmpty()) {
            return fallbackMessage;
        }

        String lowerQ = question.toLowerCase();
        ScoredChunk topChunk = retrievedChunks.get(0);
        String topDoc = topChunk.getChunk().getDocument().getFilename();

        StringBuilder bot = new StringBuilder();

        // Conversational greeting and context introduction tailored to topic intent
        if (lowerQ.contains("sick") || lowerQ.contains("leave") || lowerQ.contains("doctor") || lowerQ.contains("medical")) {
            if (lowerQ.contains("what is") || lowerQ.contains("explain") || lowerQ.contains("tell me")) {
                bot.append("According to our corporate HR policy (**").append(topDoc).append("**):\n\n");
                bot.append("**Extended Sick Leave** is an official corporate medical leave benefit available to eligible regular full-time employees recovering from prolonged illnesses or non-work-related medical conditions.\n\n");
            } else {
                bot.append("Here is how you can apply for and take extended sick leave under our corporate policy (**").append(topDoc).append("**):\n\n");
            }
            bot.append("**Key Policy Rules & Requirements:**\n");
        } else if (lowerQ.contains("refund") || lowerQ.contains("chargeback") || lowerQ.contains("reimburse")) {
            if (lowerQ.contains("what is") || lowerQ.contains("explain") || lowerQ.contains("tell me")) {
                bot.append("According to our corporate procedure (**").append(topDoc).append("**):\n\n");
                bot.append("The **Customer Refund and Chargeback SOP** governs the eligibility criteria, approval thresholds, and processing workflows for issuing customer reimbursements.\n\n");
            } else {
                bot.append("Here is how you can process and work on customer refunds according to our standard operating procedure (**").append(topDoc).append("**):\n\n");
            }
            bot.append("**Step-by-Step Procedure & Approval Limits:**\n");
        } else if (lowerQ.contains("vpn") || lowerQ.contains("remote") || lowerQ.contains("tunnel") || lowerQ.contains("mfa") || lowerQ.contains("inactivity")) {
            bot.append("According to our corporate IT security protocol (**").append(topDoc).append("**):\n\n");
            bot.append("**Remote Access & VPN Guidelines:**\n");
        } else {
            bot.append("Based on our verified corporate SOP (**").append(topDoc).append("**):\n\n");
            bot.append("**Documented Guidelines:**\n");
        }

        // Deduplicate and synthesize clear, bulleted excerpts
        Set<String> addedSnippets = new LinkedHashSet<>();
        for (ScoredChunk sc : retrievedChunks) {
            if (sc.getScore() >= similarityThreshold) {
                String snippet = sc.getMostRelevantSnippet().trim();
                // Clean leading bullet markers if present
                String clean = snippet.replaceAll("^[\\-\\•*]\\s*", "");
                if (!clean.isEmpty() && !addedSnippets.contains(clean)) {
                    addedSnippets.add(clean);
                    bot.append("• ").append(clean).append("\n");
                }
            }
        }

        return bot.toString();
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public String getFallbackMessage() {
        return fallbackMessage;
    }
}
