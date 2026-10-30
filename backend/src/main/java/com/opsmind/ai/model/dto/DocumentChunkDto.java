package com.opsmind.ai.model.dto;

import com.opsmind.ai.model.entity.DocumentChunkEntity;

import java.util.UUID;

public class DocumentChunkDto {
    private UUID id;
    private Integer pageNumber;
    private Integer chunkIndex;
    private String content;
    private Integer startChar;
    private Integer endChar;

    public DocumentChunkDto() {
    }

    public DocumentChunkDto(DocumentChunkEntity entity) {
        this.id = entity.getId();
        this.pageNumber = entity.getPageNumber();
        this.chunkIndex = entity.getChunkIndex();
        this.content = entity.getContent();
        this.startChar = entity.getStartChar();
        this.endChar = entity.getEndChar();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStartChar() {
        return startChar;
    }

    public void setStartChar(Integer startChar) {
        this.startChar = startChar;
    }

    public Integer getEndChar() {
        return endChar;
    }

    public void setEndChar(Integer endChar) {
        this.endChar = endChar;
    }
}
