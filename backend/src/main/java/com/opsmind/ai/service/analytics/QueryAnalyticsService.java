package com.opsmind.ai.service.analytics;

import com.opsmind.ai.model.dto.analytics.AnalyticsOverviewDto;
import com.opsmind.ai.model.dto.analytics.KnowledgeGapDto;
import com.opsmind.ai.model.dto.analytics.TopDocumentDto;
import com.opsmind.ai.model.dto.analytics.TopicMetricDto;
import com.opsmind.ai.model.entity.QueryLogEntity;
import com.opsmind.ai.model.entity.TopicNodeEntity;
import com.opsmind.ai.repository.CitationRepository;
import com.opsmind.ai.repository.DocumentRepository;
import com.opsmind.ai.repository.QueryLogRepository;
import com.opsmind.ai.repository.TopicNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(QueryAnalyticsService.class);

    private final QueryLogRepository queryLogRepository;
    private final CitationRepository citationRepository;
    private final DocumentRepository documentRepository;
    private final TopicNodeRepository topicNodeRepository;

    public QueryAnalyticsService(
            QueryLogRepository queryLogRepository,
            CitationRepository citationRepository,
            DocumentRepository documentRepository,
            TopicNodeRepository topicNodeRepository) {
        this.queryLogRepository = queryLogRepository;
        this.citationRepository = citationRepository;
        this.documentRepository = documentRepository;
        this.topicNodeRepository = topicNodeRepository;
    }

    /**
     * Calculates the executive overview metrics for the OpsMind AI Neural Brain dashboard.
     */
    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview() {
        long totalQueries = queryLogRepository.countTotalQueries();
        long guardrailTriggeredCount = queryLogRepository.countGuardrailTriggered();
        long answeredQueries = Math.max(0, totalQueries - guardrailTriggeredCount);

        double guardrailTriggerRate = 0.0;
        if (totalQueries > 0) {
            guardrailTriggerRate = Math.round(((double) guardrailTriggeredCount / totalQueries * 100.0) * 10.0) / 10.0;
        }

        Double rawAvgConfidence = queryLogRepository.getAverageConfidenceScore();
        double avgConfidence = rawAvgConfidence != null ? Math.round(rawAvgConfidence * 100.0) / 100.0 : 0.0;

        Double rawAvgLatency = queryLogRepository.getAverageLatencyMs();
        double avgLatency = rawAvgLatency != null ? Math.round(rawAvgLatency * 10.0) / 10.0 : 0.0;

        long totalDocuments = documentRepository.countTotalDocuments();
        long totalChunks = documentRepository.sumTotalChunks();

        return new AnalyticsOverviewDto(
                totalQueries,
                answeredQueries,
                guardrailTriggeredCount,
                guardrailTriggerRate,
                guardrailTriggeredCount, // Knowledge gaps equal ungrounded queries
                avgConfidence,
                avgLatency,
                totalDocuments,
                totalChunks
        );
    }

    /**
     * Aggregates the most cited corporate SOP documents.
     */
    @Transactional(readOnly = true)
    public List<TopDocumentDto> getTopCitedDocuments() {
        List<Object[]> rawList = citationRepository.findTopCitedDocuments();
        if (rawList.isEmpty()) {
            return List.of();
        }

        long totalCitations = 0;
        for (Object[] row : rawList) {
            Number count = (Number) row[1];
            totalCitations += count.longValue();
        }

        List<TopDocumentDto> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String filename = (String) row[0];
            long count = ((Number) row[1]).longValue();
            double percentage = totalCitations > 0
                    ? Math.round(((double) count / totalCitations * 100.0) * 10.0) / 10.0
                    : 0.0;

            result.add(new TopDocumentDto(filename, count, percentage));
        }

        return result;
    }

    /**
     * Retrieves unanswered queries flagged as Knowledge Gaps.
     */
    @Transactional(readOnly = true)
    public List<KnowledgeGapDto> getKnowledgeGaps(int limit) {
        List<QueryLogEntity> gaps = queryLogRepository.findByGuardrailTriggeredTrueOrderByCreatedAtDesc();
        return gaps.stream()
                .limit(limit > 0 ? limit : 20)
                .map(KnowledgeGapDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Calculates topic query distribution across all canonical topics.
     */
    @Transactional(readOnly = true)
    public List<TopicMetricDto> getTopicBreakdown() {
        List<TopicNodeEntity> nodes = topicNodeRepository.findAllByOrderByQueryFrequencyDesc();
        int totalQueryTraffic = nodes.stream()
                .mapToInt(n -> n.getQueryFrequency() != null ? n.getQueryFrequency() : 0)
                .sum();

        return nodes.stream()
                .map(n -> {
                    int count = n.getQueryFrequency() != null ? n.getQueryFrequency() : 0;
                    int docCount = n.getAssociatedDocumentCount() != null ? n.getAssociatedDocumentCount() : 0;
                    double pct = totalQueryTraffic > 0
                            ? Math.round(((double) count / totalQueryTraffic * 100.0) * 10.0) / 10.0
                            : 0.0;
                    return new TopicMetricDto(n.getName(), n.getCategory(), count, docCount, pct);
                })
                .collect(Collectors.toList());
    }
}
