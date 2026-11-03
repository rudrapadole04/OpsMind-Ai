package com.opsmind.ai.controller;

import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.service.graph.KnowledgeGraphService;
import com.opsmind.ai.service.ingestion.DocumentIngestionService;
import com.opsmind.ai.service.rag.RagQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private RagQueryService ragQueryService;

    @BeforeEach
    void setUp() {
        ingestionService.seedSampleSops();
        knowledgeGraphService.buildOrSyncGraph();
    }

    @Test
    @DisplayName("GET /api/analytics/overview should return 200 OK with dashboard metrics")
    void shouldReturnAnalyticsOverview() throws Exception {
        ragQueryService.processQuery(new QueryRequest("What is the refund approval threshold?"));

        mockMvc.perform(get("/api/analytics/overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDocuments", equalTo(3)))
                .andExpect(jsonPath("$.totalQueries", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.averageLatencyMs", greaterThanOrEqualTo(0.0)));
    }

    @Test
    @DisplayName("GET /api/analytics/top-documents should return 200 OK with document citation rankings")
    void shouldReturnTopDocuments() throws Exception {
        ragQueryService.processQuery(new QueryRequest("How do I process a customer refund?"));

        mockMvc.perform(get("/api/analytics/top-documents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].filename").isNotEmpty())
                .andExpect(jsonPath("$[0].citationCount", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/analytics/knowledge-gaps should return 200 OK with ungrounded questions")
    void shouldReturnKnowledgeGaps() throws Exception {
        ragQueryService.processQuery(new QueryRequest("What is the protocol for drone delivery?"));

        mockMvc.perform(get("/api/analytics/knowledge-gaps?limit=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].question").isNotEmpty())
                .andExpect(jsonPath("$[0].suggestedDepartment").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/analytics/topics should return 200 OK with topic breakdown")
    void shouldReturnTopicBreakdown() throws Exception {
        mockMvc.perform(get("/api/analytics/topics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[0].topicName").isNotEmpty())
                .andExpect(jsonPath("$[0].category").isNotEmpty());
    }
}
