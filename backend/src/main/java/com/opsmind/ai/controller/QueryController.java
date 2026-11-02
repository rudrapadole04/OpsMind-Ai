package com.opsmind.ai.controller;

import com.opsmind.ai.model.dto.QueryRequest;
import com.opsmind.ai.model.dto.QueryResponse;
import com.opsmind.ai.service.rag.RagQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final RagQueryService ragQueryService;

    public QueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping
    public ResponseEntity<QueryResponse> submitQuery(@Valid @RequestBody QueryRequest request) {
        QueryResponse response = ragQueryService.processQuery(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QueryResponse>> getQueryHistory() {
        return ResponseEntity.ok(ragQueryService.getQueryHistory());
    }

    @GetMapping("/knowledge-gaps")
    public ResponseEntity<List<QueryResponse>> getKnowledgeGaps() {
        return ResponseEntity.ok(ragQueryService.getKnowledgeGaps());
    }
}
