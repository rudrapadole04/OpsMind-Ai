package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.model.dto.QueryResponse;
import com.opsmind.ai.model.entity.QueryLogEntity;
import com.opsmind.ai.repository.QueryLogRepository;
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
class RagQueryIntegrationTest {

    @Autowired
    private RagQueryService ragQueryService;

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private QueryLogRepository queryLogRepository;

    @BeforeEach
    void setUp() {
        ingestionService.seedSampleSops();
    }

    @Test
    @DisplayName("Should retrieve accurate cited answer for refund policy query")
    void shouldAnswerRefundQueryWithPrecisionCitation() {
        QueryRequest request = new QueryRequest("How to process a refund?");
        QueryResponse response = ragQueryService.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getGuardrailTriggered()).isFalse();
        assertThat(response.getConfidenceScore()).isGreaterThanOrEqualTo(0.60);
        assertThat(response.getAnswer()).contains("Customer_Refund_and_Chargeback_SOP.pdf");
        assertThat(response.getCitations()).isNotEmpty();

        var firstCitation = response.getCitations().get(0);
        assertThat(firstCitation.getSourceFilename()).isEqualTo("Customer_Refund_and_Chargeback_SOP.pdf");
        assertThat(firstCitation.getPageNumber()).isEqualTo(1);
        assertThat(firstCitation.getExcerpt()).contains("processing customer refunds");
    }

    @Test
    @DisplayName("Should retrieve accurate cited answer for sick leave policy query")
    void shouldAnswerSickLeaveQueryWithPrecisionCitation() {
        QueryRequest request = new QueryRequest("What is the policy for extended sick leave and doctor note?");
        QueryResponse response = ragQueryService.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getGuardrailTriggered()).isFalse();
        assertThat(response.getConfidenceScore()).isGreaterThanOrEqualTo(0.60);
        assertThat(response.getAnswer()).contains("HR_Extended_Sick_Leave_Policy.pdf");
        assertThat(response.getCitations()).isNotEmpty();

        var firstCitation = response.getCitations().get(0);
        assertThat(firstCitation.getSourceFilename()).isEqualTo("HR_Extended_Sick_Leave_Policy.pdf");
        assertThat(firstCitation.getPageNumber()).isEqualTo(1);
        assertThat(firstCitation.getExcerpt()).contains("Extended Medical Sick Leave");
    }

    @Test
    @DisplayName("Should trigger Hallucination Guardrail and log knowledge gap for out-of-domain query")
    void shouldTriggerGuardrailForIrrelevantQueryAndLogGap() {
        QueryRequest request = new QueryRequest("How do I bake chocolate chip cookies with sourdough?");
        QueryResponse response = ragQueryService.processQuery(request);

        assertThat(response).isNotNull();
        assertThat(response.getGuardrailTriggered()).isTrue();
        assertThat(response.getAnswer()).isEqualTo("I don't know based on the provided corporate SOPs.");
        assertThat(response.getCitations()).isEmpty();

        // Verify that the query was audited into database as a Knowledge Gap
        List<QueryLogEntity> knowledgeGaps = queryLogRepository.findByGuardrailTriggeredTrueOrderByCreatedAtDesc();
        assertThat(knowledgeGaps).isNotEmpty();
        assertThat(knowledgeGaps.get(0).getUserQuestion()).contains("chocolate chip cookies");
        assertThat(knowledgeGaps.get(0).getGuardrailTriggered()).isTrue();
    }
}
