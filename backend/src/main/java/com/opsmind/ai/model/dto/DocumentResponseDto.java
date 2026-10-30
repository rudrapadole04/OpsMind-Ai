package com.opsmind.ai.model.dto;

import com.opsmind.ai.model.entity.DocumentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DocumentResponseDto {
    private UUID id;
    private String filename;
    private String fileType;
    private Long fileSize;
    private Integer totalPages;
    private Integer totalChunks;
    private String status;
    private String summary;
    private LocalDateTime uploadedAt;
    private List<DocumentChunkDto> chunks;

    public DocumentResponseDto() {
    }

    public DocumentResponseDto(DocumentEntity entity, boolean includeChunks) {
        this.id = entity.getId();
        this.filename = entity.getFilename();
        this.fileType = entity.getFileType();
        this.fileSize = entity.getFileSize();
        this.totalPages = entity.getTotalPages();
        this.totalChunks = entity.getTotalChunks();
        this.status = entity.getStatus();
        this.summary = entity.getSummary();
        this.uploadedAt = entity.getUploadedAt();
        if (includeChunks && entity.getChunks() != null) {
            this.chunks = entity.getChunks().stream()
                    .map(DocumentChunkDto::new)
                    .collect(Collectors.toList());
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public List<DocumentChunkDto> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunkDto> chunks) {
        this.chunks = chunks;
    }
}
