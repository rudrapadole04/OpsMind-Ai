package com.opsmind.ai.model.dto.graph;

import com.opsmind.ai.model.entity.TopicEdgeEntity;

import java.util.UUID;

public class GraphEdgeDto {

    private UUID id;
    private UUID sourceId;
    private String sourceName;
    private UUID targetId;
    private String targetName;
    private Double weight;
    private String relationshipType;

    public GraphEdgeDto() {
    }

    public GraphEdgeDto(UUID id, UUID sourceId, String sourceName, UUID targetId, String targetName, Double weight, String relationshipType) {
        this.id = id;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.targetId = targetId;
        this.targetName = targetName;
        this.weight = weight;
        this.relationshipType = relationshipType;
    }

    public GraphEdgeDto(TopicEdgeEntity entity) {
        this.id = entity.getId();
        this.sourceId = entity.getSourceNode() != null ? entity.getSourceNode().getId() : null;
        this.sourceName = entity.getSourceNode() != null ? entity.getSourceNode().getName() : "";
        this.targetId = entity.getTargetNode() != null ? entity.getTargetNode().getId() : null;
        this.targetName = entity.getTargetNode() != null ? entity.getTargetNode().getName() : "";
        this.weight = entity.getWeight() != null ? Math.round(entity.getWeight() * 100.0) / 100.0 : 1.0;
        this.relationshipType = entity.getRelationshipType();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}
