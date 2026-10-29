package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.QueryLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QueryLogRepository extends JpaRepository<QueryLogEntity, UUID> {

    List<QueryLogEntity> findByGuardrailTriggeredTrueOrderByCreatedAtDesc();

    List<QueryLogEntity> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(q) FROM QueryLogEntity q WHERE q.guardrailTriggered = true")
    long countGuardrailTriggered();

    @Query("SELECT COUNT(q) FROM QueryLogEntity q")
    long countTotalQueries();

    @Query("SELECT AVG(q.confidenceScore) FROM QueryLogEntity q WHERE q.confidenceScore IS NOT NULL")
    Double getAverageConfidenceScore();

    @Query("SELECT AVG(q.latencyMs) FROM QueryLogEntity q WHERE q.latencyMs IS NOT NULL")
    Double getAverageLatencyMs();
}
