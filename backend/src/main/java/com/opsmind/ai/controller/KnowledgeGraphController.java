package com.opsmind.ai.controller;

import com.opsmind.ai.model.dto.graph.GraphNodeDto;
import com.opsmind.ai.model.dto.graph.KnowledgeGraphDto;
import com.opsmind.ai.service.graph.KnowledgeGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/graph")
@CrossOrigin(origins = "*")
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    public KnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        this.knowledgeGraphService = knowledgeGraphService;
    }

    @GetMapping
    public ResponseEntity<KnowledgeGraphDto> getKnowledgeGraph() {
        KnowledgeGraphDto graph = knowledgeGraphService.getKnowledgeGraph();
        return ResponseEntity.ok(graph);
    }

    @PostMapping("/sync")
    public ResponseEntity<KnowledgeGraphDto> syncKnowledgeGraph() {
        KnowledgeGraphDto synced = knowledgeGraphService.buildOrSyncGraph();
        return ResponseEntity.ok(synced);
    }

    @GetMapping("/top-topics")
    public ResponseEntity<List<GraphNodeDto>> getTopTopics(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        List<GraphNodeDto> topics = knowledgeGraphService.getTopTopics(limit);
        return ResponseEntity.ok(topics);
    }
}
