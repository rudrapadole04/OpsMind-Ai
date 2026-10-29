package com.opsmind.ai.model.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
    name = "document_chunks",
    indexes = {
        @Index(name = "idx_chunk_doc_page", columnList = "document_id, page_number"),
        @Index(name = "idx_chunk_doc_idx", columnList = "document_id, chunk_index")
    }
)
public class DocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "start_char")
    private Integer startChar;

    @Column(name = "end_char")
    private Integer endChar;

    @Column(name = "embedding_id")
    private String embeddingId;

    public DocumentChunkEntity() {
    }

    public DocumentChunkEntity(Integer pageNumber, Integer chunkIndex, String content, Integer startChar, Integer endChar) {
        this.pageNumber = pageNumber;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.startChar = startChar;
        this.endChar = endChar;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
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

    public String getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(String embeddingId) {
        this.embeddingId = embeddingId;
    }
}
