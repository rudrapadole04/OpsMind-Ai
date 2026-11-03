package com.opsmind.ai.model.dto.graph;

import com.opsmind.ai.model.entity.TopicNodeEntity;

import java.util.UUID;

public class GraphNodeDto {

    private UUID id;
    private String name;
    private String category;
    private Integer queryFrequency;
    private Integer associatedDocumentCount;
    private Double importance;

    public GraphNodeDto() {
    }

    public GraphNodeDto(UUID id, String name, String category, Integer queryFrequency, Integer associatedDocumentCount, Double importance) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.queryFrequency = queryFrequency;
        this.associatedDocumentCount = associatedDocumentCount;
        this.importance = importance;
    }

    public GraphNodeDto(TopicNodeEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.category = entity.getCategory();
        this.queryFrequency = entity.getQueryFrequency() != null ? entity.getQueryFrequency() : 0;
        this.associatedDocumentCount = entity.getAssociatedDocumentCount() != null ? entity.getAssociatedDocumentCount() : 0;
        // Importance metric combines document coverage and query traffic
        this.importance = Math.round((this.associatedDocumentCount * 1.5 + this.queryFrequency * 2.0) * 100.0) / 100.0;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQueryFrequency() {
        return queryFrequency;
    }

    public void setQueryFrequency(Integer queryFrequency) {
        this.queryFrequency = queryFrequency;
    }

    public Integer getAssociatedDocumentCount() {
        return associatedDocumentCount;
    }

    public void setAssociatedDocumentCount(Integer associatedDocumentCount) {
        this.associatedDocumentCount = associatedDocumentCount;
    }

    public Double getImportance() {
        return importance;
    }

    public void setImportance(Double importance) {
        this.importance = importance;
    }
}
