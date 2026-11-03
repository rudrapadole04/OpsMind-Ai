package com.opsmind.ai.service.analytics;

import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.model.dto.analytics.AnalyticsOverviewDto;
import com.opsmind.ai.model.dto.analytics.KnowledgeGapDto;
import com.opsmind.ai.model.dto.analytics.TopDocumentDto;
import com.opsmind.ai.model.dto.analytics.TopicMetricDto;
import com.opsmind.ai.service.graph.KnowledgeGraphService;
import com.opsmind.ai.service.ingestion.DocumentIngestionService;
import com.opsmind.ai.service.rag.RagQueryService;
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
class QueryAnalyticsServiceTest {

    @Autowired
    private QueryAnalyticsService analyticsService;

    @Autowired
    private RagQueryService ragQueryService;

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @BeforeEach
    void setUp() {
        ingestionService.seedSampleSops();
        knowledgeGraphService.buildOrSyncGraph();
    }

    @Test
    @DisplayName("Should aggregate dashboard overview metrics correctly")
    void shouldReturnAccurateDashboardOverview() {
        // Run 1 valid in-domain query
        ragQueryService.processQuery(new QueryRequest("How do I process a customer refund?"));

        // Run 1 out-of-domain query (triggers guardrail)
        ragQueryService.processQuery(new QueryRequest("How do I fix a flat tire on a bicycle?"));

        AnalyticsOverviewDto overview = analyticsService.getOverview();

        assertThat(overview).isNotNull();
        assertThat(overview.getTotalQueries()).isGreaterThanOrEqualTo(2);
        assertThat(overview.getGuardrailTriggeredCount()).isGreaterThanOrEqualTo(1);
        assertThat(overview.getAnsweredQueries()).isGreaterThanOrEqualTo(1);
        assertThat(overview.getGuardrailTriggerRate()).isGreaterThan(0.0);
        assertThat(overview.getTotalDocuments()).isEqualTo(3);
        assertThat(overview.getTotalChunks()).isGreaterThanOrEqualTo(15);
        assertThat(overview.getAverageLatencyMs()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    @DisplayName("Should identify top cited documents and citation share percentages")
    void shouldReturnTopCitedDocuments() {
        ragQueryService.processQuery(new QueryRequest("How do I process a customer refund?"));

        List<TopDocumentDto> topDocs = analyticsService.getTopCitedDocuments();
        assertThat(topDocs).isNotEmpty();

        TopDocumentDto top = topDocs.get(0);
        assertThat(top.getFilename()).contains(".pdf");
        assertThat(top.getCitationCount()).isGreaterThan(0);
        assertThat(top.getCitationPercentage()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("Should retrieve knowledge gaps with department categorization")
    void shouldReturnKnowledgeGapsWithDepartments() {
        ragQueryService.processQuery(new QueryRequest("How do I submit travel expense reimbursement invoices?"));

        List<KnowledgeGapDto> gaps = analyticsService.getKnowledgeGaps(10);
        assertThat(gaps).isNotEmpty();

        KnowledgeGapDto gap = gaps.stream()
                .filter(g -> g.getQuestion().contains("travel expense"))
                .findFirst().orElseThrow();

        assertThat(gap.getQuestion()).contains("travel expense");
        assertThat(gap.getSuggestedDepartment()).isEqualTo("Finance & Billing");
    }

    @Test
    @DisplayName("Should calculate topic breakdown metrics")
    void shouldReturnTopicBreakdown() {
        List<TopicMetricDto> breakdown = analyticsService.getTopicBreakdown();

        assertThat(breakdown).isNotEmpty();
        assertThat(breakdown.size()).isGreaterThanOrEqualTo(10);
        assertThat(breakdown.get(0).getTopicName()).isNotEmpty();
        assertThat(breakdown.get(0).getCategory()).isNotEmpty();
    }
}
