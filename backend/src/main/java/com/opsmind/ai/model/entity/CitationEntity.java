package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
    name = "citations",
    indexes = {
        @Index(name = "idx_citation_query", columnList = "query_log_id"),
        @Index(name = "idx_citation_chunk", columnList = "document_chunk_id")
    }
)
public class CitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_log_id", nullable = false)
    private QueryLogEntity queryLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_chunk_id")
    private DocumentChunkEntity documentChunk;

    @Column(name = "source_filename", nullable = false)
    private String sourceFilename;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "excerpt", columnDefinition = "TEXT", nullable = false)
    private String excerpt;

    @Column(name = "relevance_score")
    private Double relevanceScore;

    public CitationEntity() {
    }

    public CitationEntity(String sourceFilename, Integer pageNumber, String excerpt, Double relevanceScore) {
        this.sourceFilename = sourceFilename;
        this.pageNumber = pageNumber;
        this.excerpt = excerpt;
        this.relevanceScore = relevanceScore;
    }

    public CitationEntity(DocumentChunkEntity documentChunk, String sourceFilename, Integer pageNumber, String excerpt, Double relevanceScore) {
        this.documentChunk = documentChunk;
        this.sourceFilename = sourceFilename;
        this.pageNumber = pageNumber;
        this.excerpt = excerpt;
        this.relevanceScore = relevanceScore;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public QueryLogEntity getQueryLog() {
        return queryLog;
    }

    public void setQueryLog(QueryLogEntity queryLog) {
        this.queryLog = queryLog;
    }

    public DocumentChunkEntity getDocumentChunk() {
        return documentChunk;
    }

    public void setDocumentChunk(DocumentChunkEntity documentChunk) {
        this.documentChunk = documentChunk;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public Double getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
}
