package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "total_chunks")
    private Integer totalChunks;

    @Column(nullable = false)
    private String status; // INDEXED, PROCESSING, FAILED

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentChunkEntity> chunks = new ArrayList<>();

    public DocumentEntity() {
    }

    @PrePersist
    public void prePersist() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PROCESSING";
        }
    }

    public DocumentEntity(String filename, String fileType, Long fileSize, Integer totalPages) {
        this.filename = filename;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.totalPages = totalPages;
        this.totalChunks = 0;
        this.status = "PROCESSING";
        this.uploadedAt = LocalDateTime.now();
    }

    public void addChunk(DocumentChunkEntity chunk) {
        chunks.add(chunk);
        chunk.setDocument(this);
    }

    public void removeChunk(DocumentChunkEntity chunk) {
        chunks.remove(chunk);
        chunk.setDocument(null);
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

    public List<DocumentChunkEntity> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunkEntity> chunks) {
        this.chunks = chunks;
    }
}
