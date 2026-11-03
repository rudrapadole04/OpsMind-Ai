package com.opsmind.ai.controller;

import com.opsmind.ai.service.graph.KnowledgeGraphService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KnowledgeGraphControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("GET /api/graph should return 200 OK with graph nodes and edges")
    void shouldReturnKnowledgeGraph() throws Exception {
        mockMvc.perform(get("/api/graph")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNodes", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.totalEdges", greaterThanOrEqualTo(9)))
                .andExpect(jsonPath("$.nodes", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$.edges", hasSize(greaterThanOrEqualTo(9))))
                .andExpect(jsonPath("$.nodes[0].name").isNotEmpty())
                .andExpect(jsonPath("$.nodes[0].category").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/graph/sync should re-sync graph topology and return 200 OK")
    void shouldSyncKnowledgeGraph() throws Exception {
        mockMvc.perform(post("/api/graph/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNodes", greaterThanOrEqualTo(10)))
                .andExpect(jsonPath("$.totalEdges", greaterThanOrEqualTo(9)));
    }

    @Test
    @DisplayName("GET /api/graph/top-topics should return list of top queried topics")
    void shouldReturnTopTopics() throws Exception {
        mockMvc.perform(get("/api/graph/top-topics?limit=5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name").isNotEmpty());
    }
}
