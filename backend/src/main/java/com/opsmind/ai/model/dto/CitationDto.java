package com.opsmind.ai.model.dto;

import com.opsmind.ai.model.entity.CitationEntity;

import java.util.UUID;

public class CitationDto {
    private UUID id;
    private String sourceFilename;
    private Integer pageNumber;
    private String excerpt;
    private Double relevanceScore;

    public CitationDto() {
    }

    public CitationDto(String sourceFilename, Integer pageNumber, String excerpt, Double relevanceScore) {
        this.sourceFilename = sourceFilename;
        this.pageNumber = pageNumber;
        this.excerpt = excerpt;
        this.relevanceScore = relevanceScore;
    }

    public CitationDto(CitationEntity entity) {
        this.id = entity.getId();
        this.sourceFilename = entity.getSourceFilename();
        this.pageNumber = entity.getPageNumber();
        this.excerpt = entity.getExcerpt();
        this.relevanceScore = entity.getRelevanceScore();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
