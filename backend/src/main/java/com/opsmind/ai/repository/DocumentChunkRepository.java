package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.DocumentChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunkEntity, UUID> {

    List<DocumentChunkEntity> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

    List<DocumentChunkEntity> findByDocumentIdAndPageNumber(UUID documentId, Integer pageNumber);

    long countByDocumentId(UUID documentId);

    void deleteByDocumentId(UUID documentId);
}
