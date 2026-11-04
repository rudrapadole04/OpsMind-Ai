package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HibernatePersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private QueryLogRepository queryLogRepository;

    @Autowired
    private CitationRepository citationRepository;

    @Autowired
    private TopicNodeRepository topicNodeRepository;

    @Autowired
    private TopicEdgeRepository topicEdgeRepository;

    @Test
    @DisplayName("Should cascade persist Document and its DocumentChunks")
    void shouldCascadePersistDocumentAndChunks() {
        // Arrange
        DocumentEntity doc = new DocumentEntity("HR_Sick_Leave_Policy.pdf", "application/pdf", 1048576L, 12);
        doc.setStatus("INDEXED");
        doc.setSummary("HR guidelines regarding extended medical leaves and compensation.");

        DocumentChunkEntity chunk1 = new DocumentChunkEntity(1, 0, "Employees with more than 3 consecutive sick days require a doctor note.", 0, 85);
        DocumentChunkEntity chunk2 = new DocumentChunkEntity(2, 1, "Extended sick leave grants up to 12 weeks of statutory medical benefit.", 86, 170);

        doc.addChunk(chunk1);
        doc.addChunk(chunk2);
        doc.setTotalChunks(2);

        // Act
        DocumentEntity savedDoc = documentRepository.save(doc);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(savedDoc.getId()).isNotNull();
        Optional<DocumentEntity> retrieved = documentRepository.findById(savedDoc.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getFilename()).isEqualTo("HR_Sick_Leave_Policy.pdf");

        List<DocumentChunkEntity> retrievedChunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(savedDoc.getId());
        assertThat(retrievedChunks).hasSize(2);
        assertThat(retrievedChunks.get(0).getPageNumber()).isEqualTo(1);
        assertThat(retrievedChunks.get(0).getContent()).contains("doctor note");
        assertThat(retrievedChunks.get(1).getPageNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should cascade delete DocumentChunks when Document is deleted (orphanRemoval)")
    void shouldCascadeDeleteChunksWhenDocumentDeleted() {
        // Arrange
        DocumentEntity doc = new DocumentEntity("Refund_SOP.pdf", "application/pdf", 524288L, 4);
        DocumentChunkEntity chunk = new DocumentChunkEntity(1, 0, "Refunds under $50 can be approved directly by Tier 1 support.", 0, 68);
        doc.addChunk(chunk);
        doc.setTotalChunks(1);

        DocumentEntity savedDoc = documentRepository.save(doc);
        entityManager.flush();
        UUID docId = savedDoc.getId();

        // Act
        documentRepository.delete(savedDoc);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(documentRepository.findById(docId)).isEmpty();
        assertThat(documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(docId)).isEmpty();
    }

    @Test
    @DisplayName("Should log employee queries and distinguish Hallucination Guardrail triggers (Knowledge Gaps)")
    void shouldTrackQueryLogsAndGuardrailTriggers() {
        // Normal grounded query
        QueryLogEntity validQuery = new QueryLogEntity(
                "How do I process a refund?",
                "Refunds under $50 are approved by Tier 1 support. Over $50 requires manager sign-off.",
                0.92,
                false,
                142L
        );
        CitationEntity citation = new CitationEntity("Refund_SOP.pdf", 1, "Refunds under $50 can be approved directly", 0.94);
        validQuery.addCitation(citation);

        // Out-of-domain query triggering Hallucination Guardrail
        QueryLogEntity guardrailQuery = new QueryLogEntity(
                "What is the secret recipe for cafeteria cookies?",
                "I am not able to answer this based on the provided corporate SOPs.",
                0.12,
                true,
                88L
        );

        queryLogRepository.save(validQuery);
        queryLogRepository.save(guardrailQuery);
        entityManager.flush();
        entityManager.clear();

        // Assert
        List<QueryLogEntity> knowledgeGaps = queryLogRepository.findByGuardrailTriggeredTrueOrderByCreatedAtDesc();
        assertThat(knowledgeGaps).hasSize(1);
        assertThat(knowledgeGaps.get(0).getUserQuestion()).contains("cafeteria cookies");
        assertThat(knowledgeGaps.get(0).getSynthesizedAnswer()).isEqualTo("I am not able to answer this based on the provided corporate SOPs.");

        assertThat(queryLogRepository.countTotalQueries()).isEqualTo(2);
        assertThat(queryLogRepository.countGuardrailTriggered()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should persist Admin Knowledge Graph topic nodes and weighted relationship edges")
    void shouldPersistKnowledgeGraphNodesAndEdges() {
        // Arrange
        TopicNodeEntity hrNode = new TopicNodeEntity("Sick Leave", "HR", 15, 3);
        TopicNodeEntity financeNode = new TopicNodeEntity("Medical Reimbursement", "FINANCE", 8, 2);

        TopicNodeEntity savedHr = topicNodeRepository.save(hrNode);
        TopicNodeEntity savedFinance = topicNodeRepository.save(financeNode);

        TopicEdgeEntity edge = new TopicEdgeEntity(savedHr, savedFinance, 2.5, "POLICY_PREREQUISITE");
        topicEdgeRepository.save(edge);

        entityManager.flush();
        entityManager.clear();

        // Assert
        List<TopicNodeEntity> topNodes = topicNodeRepository.findTop15ByOrderByQueryFrequencyDesc();
        assertThat(topNodes).hasSize(2);
        assertThat(topNodes.get(0).getName()).isEqualTo("Sick Leave");

        List<TopicEdgeEntity> edges = topicEdgeRepository.findAllByOrderByWeightDesc();
        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getSourceNode().getName()).isEqualTo("Sick Leave");
        assertThat(edges.get(0).getTargetNode().getName()).isEqualTo("Medical Reimbursement");
        assertThat(edges.get(0).getWeight()).isEqualTo(2.5);
    }
}
