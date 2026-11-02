package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import com.opsmind.ai.repository.DocumentChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VectorSearchServiceTest {

    @Mock
    private DocumentChunkRepository chunkRepository;

    private VectorSearchService vectorSearchService;

    @BeforeEach
    void setUp() {
        vectorSearchService = new VectorSearchService(chunkRepository, 5);
    }

    @Test
    @DisplayName("Should retrieve most relevant chunk with high cosine similarity for matching domain query")
    void shouldFindRelevantChunk() {
        DocumentEntity refundDoc = new DocumentEntity("Refund_SOP.pdf", "application/pdf", 1000L, 2);
        DocumentChunkEntity chunk1 = new DocumentChunkEntity(1, 0,
                "Tier 1 Support Specialists are authorized to approve customer refunds under $50.00 immediately.", 0, 95);
        refundDoc.addChunk(chunk1);

        DocumentEntity vpnDoc = new DocumentEntity("VPN_SOP.pdf", "application/pdf", 1000L, 1);
        DocumentChunkEntity chunk2 = new DocumentChunkEntity(1, 0,
                "Remote employees must connect to the GlobalProtect corporate VPN gateway.", 0, 75);
        vpnDoc.addChunk(chunk2);

        when(chunkRepository.findAll()).thenReturn(List.of(chunk1, chunk2));

        List<ScoredChunk> results = vectorSearchService.search("how to approve a customer refund under 50 dollars");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getChunk().getDocument().getFilename()).isEqualTo("Refund_SOP.pdf");
        assertThat(results.get(0).getScore()).isGreaterThanOrEqualTo(0.60);
        assertThat(results.get(0).getMostRelevantSnippet()).contains("refunds under $50.00");
    }

    @Test
    @DisplayName("Should return low similarity scores for out-of-domain irrelevant questions")
    void shouldScoreLowForIrrelevantQuery() {
        DocumentEntity vpnDoc = new DocumentEntity("VPN_SOP.pdf", "application/pdf", 1000L, 1);
        DocumentChunkEntity chunk = new DocumentChunkEntity(1, 0,
                "Remote employees must connect to the GlobalProtect corporate VPN gateway.", 0, 75);
        vpnDoc.addChunk(chunk);

        when(chunkRepository.findAll()).thenReturn(List.of(chunk));

        List<ScoredChunk> results = vectorSearchService.search("how do I bake a chocolate cake with vanilla frosting");

        if (!results.isEmpty()) {
            assertThat(results.get(0).getScore()).isLessThan(0.60);
        }
    }
}
