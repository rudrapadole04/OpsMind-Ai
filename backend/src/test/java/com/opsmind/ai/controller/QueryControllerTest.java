package com.opsmind.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.service.ingestion.DocumentIngestionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService.seedSampleSops();
    }

    @Test
    @DisplayName("POST /api/query with valid question should return 200 OK with citations")
    void shouldReturnQueryResponse() throws Exception {
        QueryRequest request = new QueryRequest("How do I process a refund under $50?");

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("How do I process a refund under $50?"))
                .andExpect(jsonPath("$.guardrailTriggered").value(false))
                .andExpect(jsonPath("$.confidenceScore", greaterThanOrEqualTo(0.60)))
                .andExpect(jsonPath("$.citations", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.citations[0].sourceFilename").isNotEmpty())
                .andExpect(jsonPath("$.citations[0].pageNumber", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/query with blank question should return 400 Bad Request")
    void shouldRejectBlankQuestion() throws Exception {
        QueryRequest request = new QueryRequest("   ");

        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/query/history should return recent query logs")
    void shouldReturnQueryHistory() throws Exception {
        // Send a query first
        QueryRequest request = new QueryRequest("What is the sick leave policy?");
        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/query/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/query/knowledge-gaps should return questions where guardrail was triggered")
    void shouldReturnKnowledgeGaps() throws Exception {
        // Send an out-of-domain question that triggers guardrail
        QueryRequest outOfDomain = new QueryRequest("What is the recipe for vanilla ice cream?");
        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(outOfDomain)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guardrailTriggered").value(true))
                .andExpect(jsonPath("$.answer").value("I don't know based on the provided corporate SOPs."));

        mockMvc.perform(get("/api/query/knowledge-gaps"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].guardrailTriggered").value(true));
    }
}
