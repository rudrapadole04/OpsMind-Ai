package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
    name = "topic_nodes",
    indexes = {
        @Index(name = "idx_topic_name", columnList = "name"),
        @Index(name = "idx_topic_frequency", columnList = "query_frequency")
    }
)
public class TopicNodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "query_frequency", nullable = false)
    private Integer queryFrequency = 0;

    @Column(name = "associated_document_count", nullable = false)
    private Integer associatedDocumentCount = 0;

    public TopicNodeEntity() {
    }

    public TopicNodeEntity(String name, String category) {
        this.name = name;
        this.category = category;
        this.queryFrequency = 0;
        this.associatedDocumentCount = 0;
    }

    public TopicNodeEntity(String name, String category, Integer queryFrequency, Integer associatedDocumentCount) {
        this.name = name;
        this.category = category;
        this.queryFrequency = queryFrequency;
        this.associatedDocumentCount = associatedDocumentCount;
    }

    public void incrementQueryFrequency() {
        this.queryFrequency = (this.queryFrequency == null ? 0 : this.queryFrequency) + 1;
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
}
