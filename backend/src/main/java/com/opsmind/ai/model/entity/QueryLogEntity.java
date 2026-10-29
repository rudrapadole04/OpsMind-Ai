package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "query_logs",
    indexes = {
        @Index(name = "idx_query_guardrail", columnList = "guardrail_triggered"),
        @Index(name = "idx_query_created", columnList = "created_at")
    }
)
public class QueryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_question", nullable = false, columnDefinition = "TEXT")
    private String userQuestion;

    @Column(name = "synthesized_answer", nullable = false, columnDefinition = "TEXT")
    private String synthesizedAnswer;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "guardrail_triggered", nullable = false)
    private Boolean guardrailTriggered = false;

    @Column(name = "latency_ms")
    private Long latencyMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "queryLog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CitationEntity> citations = new ArrayList<>();

    public QueryLogEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.guardrailTriggered == null) {
            this.guardrailTriggered = false;
        }
    }

    public QueryLogEntity(String userQuestion, String synthesizedAnswer, Double confidenceScore, Boolean guardrailTriggered, Long latencyMs) {
        this.userQuestion = userQuestion;
        this.synthesizedAnswer = synthesizedAnswer;
        this.confidenceScore = confidenceScore;
        this.guardrailTriggered = guardrailTriggered;
        this.latencyMs = latencyMs;
        this.createdAt = LocalDateTime.now();
    }

    public void addCitation(CitationEntity citation) {
        citations.add(citation);
        citation.setQueryLog(this);
    }

    public void removeCitation(CitationEntity citation) {
        citations.remove(citation);
        citation.setQueryLog(null);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public void setUserQuestion(String userQuestion) {
        this.userQuestion = userQuestion;
    }

    public String getSynthesizedAnswer() {
        return synthesizedAnswer;
    }

    public void setSynthesizedAnswer(String synthesizedAnswer) {
        this.synthesizedAnswer = synthesizedAnswer;
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

    public List<CitationEntity> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationEntity> citations) {
        this.citations = citations;
    }
}
