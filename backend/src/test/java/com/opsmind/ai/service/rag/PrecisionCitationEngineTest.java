package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.CitationDto;
import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrecisionCitationEngineTest {

    private final PrecisionCitationEngine citationEngine = new PrecisionCitationEngine();

    @Test
    @DisplayName("Should build verifiable citation metadata with exact filename, page number, and quote")
    void shouldBuildCitationsCorrectly() {
        DocumentEntity doc = new DocumentEntity("HR_Sick_Leave_Policy.pdf", "application/pdf", 2000L, 3);
        DocumentChunkEntity chunk1 = new DocumentChunkEntity(1, 0,
                "For any continuous illness exceeding 3 consecutive business days, employees must submit a formal medical certificate.", 0, 118);
        doc.addChunk(chunk1);

        DocumentChunkEntity chunk2 = new DocumentChunkEntity(1, 1,
                "The certificate must confirm incapacity to work without disclosing confidential diagnoses.", 119, 210);
        doc.addChunk(chunk2);

        ScoredChunk sc1 = new ScoredChunk(chunk1, 0.88, "Employees must submit a formal medical certificate.");
        ScoredChunk sc2 = new ScoredChunk(chunk2, 0.72, "Confirm incapacity to work without disclosing confidential diagnoses.");

        List<CitationDto> citations = citationEngine.buildCitations(List.of(sc1, sc2));

        // Deduplication preserves the highest scoring chunk for the same doc and page
        assertThat(citations).hasSize(1);
        CitationDto citation = citations.get(0);
        assertThat(citation.getSourceFilename()).isEqualTo("HR_Sick_Leave_Policy.pdf");
        assertThat(citation.getPageNumber()).isEqualTo(1);
        assertThat(citation.getExcerpt()).contains("medical certificate");
        assertThat(citation.getRelevanceScore()).isEqualTo(0.88);
    }

    @Test
    @DisplayName("Should generate formatted cited answer with clickable sources")
    void shouldGenerateCitedAnswer() {
        CitationDto c = new CitationDto("Refund_SOP.pdf", 2, "Rebuttals must be submitted within 14 business days.", 0.91);
        String citedAnswer = citationEngine.generateCitedAnswer("Dispute evidence has strict deadlines.", List.of(c));

        assertThat(citedAnswer).contains("Dispute evidence has strict deadlines.");
        assertThat(citedAnswer).contains("Verified Sources & Citations:");
        assertThat(citedAnswer).contains("Refund_SOP.pdf");
        assertThat(citedAnswer).contains("Page 2");
        assertThat(citedAnswer).contains("14 business days");
    }
}
