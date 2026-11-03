package com.opsmind.ai.controller;

import com.opsmind.ai.model.dto.analytics.AnalyticsOverviewDto;
import com.opsmind.ai.model.dto.analytics.KnowledgeGapDto;
import com.opsmind.ai.model.dto.analytics.TopDocumentDto;
import com.opsmind.ai.model.dto.analytics.TopicMetricDto;
import com.opsmind.ai.service.analytics.QueryAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final QueryAnalyticsService queryAnalyticsService;

    public AnalyticsController(QueryAnalyticsService queryAnalyticsService) {
        this.queryAnalyticsService = queryAnalyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        AnalyticsOverviewDto overview = queryAnalyticsService.getOverview();
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/top-documents")
    public ResponseEntity<List<TopDocumentDto>> getTopDocuments() {
        List<TopDocumentDto> topDocs = queryAnalyticsService.getTopCitedDocuments();
        return ResponseEntity.ok(topDocs);
    }

    @GetMapping("/knowledge-gaps")
    public ResponseEntity<List<KnowledgeGapDto>> getKnowledgeGaps(
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<KnowledgeGapDto> gaps = queryAnalyticsService.getKnowledgeGaps(limit);
        return ResponseEntity.ok(gaps);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicMetricDto>> getTopicBreakdown() {
        List<TopicMetricDto> breakdown = queryAnalyticsService.getTopicBreakdown();
        return ResponseEntity.ok(breakdown);
    }
}
