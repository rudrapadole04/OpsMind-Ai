package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
    name = "topic_edges",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_edge_source_target", columnNames = {"source_node_id", "target_node_id"})
    }
)
public class TopicEdgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_node_id", nullable = false)
    private TopicNodeEntity sourceNode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_node_id", nullable = false)
    private TopicNodeEntity targetNode;

    @Column(nullable = false)
    private Double weight = 1.0;

    @Column(name = "relationship_type", nullable = false)
    private String relationshipType; // CO_OCCURRENCE, SOP_REFERENCE, TOPIC_SIMILARITY

    public TopicEdgeEntity() {
    }

    public TopicEdgeEntity(TopicNodeEntity sourceNode, TopicNodeEntity targetNode, Double weight, String relationshipType) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.weight = weight;
        this.relationshipType = relationshipType;
    }

    public void incrementWeight(double delta) {
        this.weight = (this.weight == null ? 0.0 : this.weight) + delta;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TopicNodeEntity getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(TopicNodeEntity sourceNode) {
        this.sourceNode = sourceNode;
    }

    public TopicNodeEntity getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(TopicNodeEntity targetNode) {
        this.targetNode = targetNode;
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
