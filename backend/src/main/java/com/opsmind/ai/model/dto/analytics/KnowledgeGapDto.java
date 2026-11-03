package com.opsmind.ai.model.dto.analytics;

import com.opsmind.ai.model.entity.QueryLogEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public class KnowledgeGapDto {

    private UUID id;
    private String question;
    private Double confidenceScore;
    private LocalDateTime createdAt;
    private String suggestedDepartment;

    public KnowledgeGapDto() {
    }

    public KnowledgeGapDto(UUID id, String question, Double confidenceScore, LocalDateTime createdAt, String suggestedDepartment) {
        this.id = id;
        this.question = question;
        this.confidenceScore = confidenceScore;
        this.createdAt = createdAt;
        this.suggestedDepartment = suggestedDepartment;
    }

    public KnowledgeGapDto(QueryLogEntity entity) {
        this.id = entity.getId();
        this.question = entity.getUserQuestion();
        this.confidenceScore = entity.getConfidenceScore();
        this.createdAt = entity.getCreatedAt();
        this.suggestedDepartment = inferDepartment(entity.getUserQuestion());
    }

    private String inferDepartment(String question) {
        if (question == null) return "General Operations";
        String lower = question.toLowerCase();
        if (lower.contains("refund") || lower.contains("chargeback") || lower.contains("invoice") || lower.contains("payment") || lower.contains("billing")) {
            return "Finance & Billing";
        } else if (lower.contains("sick") || lower.contains("leave") || lower.contains("hr") || lower.contains("salary") || lower.contains("vacation") || lower.contains("doctor")) {
            return "Human Resources";
        } else if (lower.contains("vpn") || lower.contains("network") || lower.contains("password") || lower.contains("security") || lower.contains("it") || lower.contains("login")) {
            return "Information Technology";
        }
        return "General Operations";
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

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSuggestedDepartment() {
        return suggestedDepartment;
    }

    public void setSuggestedDepartment(String suggestedDepartment) {
        this.suggestedDepartment = suggestedDepartment;
    }
}
