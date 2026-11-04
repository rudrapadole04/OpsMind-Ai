package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallucinationGuardrailTest {

    private HallucinationGuardrailService guardrailService;

    @BeforeEach
    void setUp() {
        PrecisionCitationEngine citationEngine = new PrecisionCitationEngine();
        guardrailService = new HallucinationGuardrailService(0.60, "I am not able to answer this based on the provided corporate SOPs.", citationEngine);
    }

    @Test
    @DisplayName("Should trigger guardrail when no relevant chunks are found")
    void shouldTriggerGuardrailWhenEmptyChunks() {
        var eval = guardrailService.evaluateAndSynthesize("What is the recipe for cookies?", Collections.emptyList());

        assertThat(eval.guardrailTriggered()).isTrue();
        assertThat(eval.synthesizedAnswer()).isEqualTo("I am not able to answer this based on the provided corporate SOPs.");
        assertThat(eval.confidenceScore()).isEqualTo(0.0);
        assertThat(eval.citations()).isEmpty();
    }

    @Test
    @DisplayName("Should trigger guardrail when top score is below 0.60 similarity threshold")
    void shouldTriggerGuardrailWhenScoreBelowThreshold() {
        DocumentEntity doc = new DocumentEntity("Finance_Policy.pdf", "application/pdf", 1000L, 1);
        DocumentChunkEntity chunk = new DocumentChunkEntity(1, 0, "Quarterly billing closes on the 28th.", 0, 36);
        doc.addChunk(chunk);

        ScoredChunk sc = new ScoredChunk(chunk, 0.42, "Quarterly billing closes on the 28th.");

        var eval = guardrailService.evaluateAndSynthesize("How do I fix a flat tire?", List.of(sc));

        assertThat(eval.guardrailTriggered()).isTrue();
        assertThat(eval.synthesizedAnswer()).isEqualTo("I am not able to answer this based on the provided corporate SOPs.");
        assertThat(eval.confidenceScore()).isEqualTo(0.42);
        assertThat(eval.citations()).isEmpty();
    }

    @Test
    @DisplayName("Should pass guardrail when top score meets or exceeds 0.60 threshold")
    void shouldPassGuardrailWhenScoreExceedsThreshold() {
        DocumentEntity doc = new DocumentEntity("Refund_SOP.pdf", "application/pdf", 1000L, 1);
        DocumentChunkEntity chunk = new DocumentChunkEntity(1, 0,
                "Tier 1 Support Specialists are authorized to approve refunds under $50.00 immediately.", 0, 86);
        doc.addChunk(chunk);

        ScoredChunk sc = new ScoredChunk(chunk, 0.85, "Tier 1 Support Specialists are authorized to approve refunds under $50.00");

        var eval = guardrailService.evaluateAndSynthesize("How to process a refund?", List.of(sc));

        assertThat(eval.guardrailTriggered()).isFalse();
        assertThat(eval.confidenceScore()).isEqualTo(0.85);
        assertThat(eval.synthesizedAnswer()).contains("Refund_SOP.pdf");
        assertThat(eval.synthesizedAnswer()).contains("Page 1");
        assertThat(eval.citations()).isNotEmpty();
    }
}
