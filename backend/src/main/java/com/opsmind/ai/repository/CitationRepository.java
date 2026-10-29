package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.CitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CitationRepository extends JpaRepository<CitationEntity, UUID> {

    List<CitationEntity> findByQueryLogId(UUID queryLogId);

    List<CitationEntity> findBySourceFilename(String sourceFilename);

    @Query("SELECT c.sourceFilename, COUNT(c) as citationCount FROM CitationEntity c GROUP BY c.sourceFilename ORDER BY citationCount DESC")
    List<Object[]> findTopCitedDocuments();
}
