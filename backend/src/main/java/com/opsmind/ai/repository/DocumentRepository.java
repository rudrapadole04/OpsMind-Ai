package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findByFilename(String filename);

    List<DocumentEntity> findByStatusOrderByUploadedAtDesc(String status);

    List<DocumentEntity> findAllByOrderByUploadedAtDesc();

    @Query("SELECT COUNT(d) FROM DocumentEntity d")
    long countTotalDocuments();

    @Query("SELECT COALESCE(SUM(d.totalPages), 0) FROM DocumentEntity d")
    int sumTotalPages();

    @Query("SELECT COALESCE(SUM(d.totalChunks), 0) FROM DocumentEntity d")
    int sumTotalChunks();
}
