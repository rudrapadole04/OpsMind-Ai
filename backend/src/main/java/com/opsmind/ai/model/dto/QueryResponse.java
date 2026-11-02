package com.opsmind.ai.model.dto;

import com.opsmind.ai.model.entity.QueryLogEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class QueryResponse {
    private UUID id;
    private String question;
    private String answer;
    private Double confidenceScore;
    private Boolean guardrailTriggered;
    private Long latencyMs;
    private LocalDateTime createdAt;
    private List<CitationDto> citations = new ArrayList<>();

    public QueryResponse() {
    }

    public QueryResponse(String question, String answer, Double confidenceScore, Boolean guardrailTriggered, Long latencyMs, List<CitationDto> citations) {
        this.question = question;
        this.answer = answer;
        this.confidenceScore = confidenceScore;
        this.guardrailTriggered = guardrailTriggered;
        this.latencyMs = latencyMs;
        this.citations = citations != null ? citations : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    public QueryResponse(QueryLogEntity entity) {
        this.id = entity.getId();
        this.question = entity.getUserQuestion();
        this.answer = entity.getSynthesizedAnswer();
        this.confidenceScore = entity.getConfidenceScore();
        this.guardrailTriggered = entity.getGuardrailTriggered();
        this.latencyMs = entity.getLatencyMs();
        this.createdAt = entity.getCreatedAt();
        if (entity.getCitations() != null) {
            this.citations = entity.getCitations().stream()
                    .map(CitationDto::new)
                    .collect(Collectors.toList());
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Boolean getGuardrailTriggered() {
        return guardrailTriggered;
    }

    public void setGuardrailTriggered(Boolean guardrailTriggered) {
        this.guardrailTriggered = guardrailTriggered;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<CitationDto> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationDto> citations) {
        this.citations = citations;
    }
}
