package com.opsmind.ai.model.dto.analytics;

public class AnalyticsOverviewDto {

    private long totalQueries;
    private long answeredQueries;
    private long guardrailTriggeredCount;
    private double guardrailTriggerRate; // percentage, e.g. 15.5%
    private long knowledgeGapCount;
    private double averageConfidenceScore; // e.g. 0.86
    private double averageLatencyMs; // e.g. 12.4 ms
    private long totalDocuments;
    private long totalChunks;

    public AnalyticsOverviewDto() {
    }

    public AnalyticsOverviewDto(
            long totalQueries,
            long answeredQueries,
            long guardrailTriggeredCount,
            double guardrailTriggerRate,
            long knowledgeGapCount,
            double averageConfidenceScore,
            double averageLatencyMs,
            long totalDocuments,
            long totalChunks) {
        this.totalQueries = totalQueries;
        this.answeredQueries = answeredQueries;
        this.guardrailTriggeredCount = guardrailTriggeredCount;
        this.guardrailTriggerRate = guardrailTriggerRate;
        this.knowledgeGapCount = knowledgeGapCount;
        this.averageConfidenceScore = averageConfidenceScore;
        this.averageLatencyMs = averageLatencyMs;
        this.totalDocuments = totalDocuments;
        this.totalChunks = totalChunks;
    }

    public long getTotalQueries() {
        return totalQueries;
    }

    public void setTotalQueries(long totalQueries) {
        this.totalQueries = totalQueries;
    }

    public long getAnsweredQueries() {
        return answeredQueries;
    }

    public void setAnsweredQueries(long answeredQueries) {
        this.answeredQueries = answeredQueries;
    }

    public long getGuardrailTriggeredCount() {
        return guardrailTriggeredCount;
    }

    public void setGuardrailTriggeredCount(long guardrailTriggeredCount) {
        this.guardrailTriggeredCount = guardrailTriggeredCount;
    }

    public double getGuardrailTriggerRate() {
        return guardrailTriggerRate;
    }

    public void setGuardrailTriggerRate(double guardrailTriggerRate) {
        this.guardrailTriggerRate = guardrailTriggerRate;
    }

    public long getKnowledgeGapCount() {
        return knowledgeGapCount;
    }

    public void setKnowledgeGapCount(long knowledgeGapCount) {
        this.knowledgeGapCount = knowledgeGapCount;
    }

    public double getAverageConfidenceScore() {
        return averageConfidenceScore;
    }

    public void setAverageConfidenceScore(double averageConfidenceScore) {
        this.averageConfidenceScore = averageConfidenceScore;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public void setAverageLatencyMs(double averageLatencyMs) {
        this.averageLatencyMs = averageLatencyMs;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public long getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(long totalChunks) {
        this.totalChunks = totalChunks;
    }
}
