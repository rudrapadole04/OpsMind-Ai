package com.opsmind.ai.service.graph;

import com.opsmind.ai.model.dto.graph.GraphNodeDto;
import com.opsmind.ai.model.dto.graph.KnowledgeGraphDto;
import com.opsmind.ai.service.ingestion.DocumentIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class KnowledgeGraphServiceTest {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService.seedSampleSops();
    }

    @Test
    @DisplayName("Should build and synchronize Knowledge Graph topology with canonical nodes and edges")
    void shouldBuildAndSyncGraph() {
        KnowledgeGraphDto graph = knowledgeGraphService.buildOrSyncGraph();

        assertThat(graph).isNotNull();
        assertThat(graph.getNodes()).isNotEmpty();
        assertThat(graph.getEdges()).isNotEmpty();
        assertThat(graph.getTotalNodes()).isGreaterThanOrEqualTo(10);
        assertThat(graph.getTotalEdges()).isGreaterThanOrEqualTo(9);

        // Check specific key topics exist
        boolean hasRefunds = graph.getNodes().stream().anyMatch(n -> n.getName().equals("Customer Refunds"));
        boolean hasVpn = graph.getNodes().stream().anyMatch(n -> n.getName().equals("VPN Remote Access"));
        boolean hasSickLeave = graph.getNodes().stream().anyMatch(n -> n.getName().equals("Extended Sick Leave"));

        assertThat(hasRefunds).isTrue();
        assertThat(hasVpn).isTrue();
        assertThat(hasSickLeave).isTrue();

        // Check document associations are computed
        GraphNodeDto refundNode = graph.getNodes().stream()
                .filter(n -> n.getName().equals("Customer Refunds"))
                .findFirst().orElseThrow();
        assertThat(refundNode.getAssociatedDocumentCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should record query topic hits and increase query frequency and edge weights")
    void shouldRecordQueryTopicHits() {
        knowledgeGraphService.buildOrSyncGraph();

        // Record a query touching Customer Refunds and Chargeback Disputes
        knowledgeGraphService.recordQueryTopicHits("How do I dispute a customer refund or chargeback?", List.of());

        KnowledgeGraphDto updatedGraph = knowledgeGraphService.getKnowledgeGraph();
        GraphNodeDto refundNode = updatedGraph.getNodes().stream()
                .filter(n -> n.getName().equals("Customer Refunds"))
                .findFirst().orElseThrow();

        assertThat(refundNode.getQueryFrequency()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should retrieve top queried topics")
    void shouldRetrieveTopTopics() {
        knowledgeGraphService.buildOrSyncGraph();

        List<GraphNodeDto> topTopics = knowledgeGraphService.getTopTopics(3);
        assertThat(topTopics).hasSize(3);
        assertThat(topTopics.get(0).getName()).isNotEmpty();
    }
}
