package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.TopicEdgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicEdgeRepository extends JpaRepository<TopicEdgeEntity, UUID> {

    @Query("SELECT e FROM TopicEdgeEntity e WHERE e.sourceNode.id = :sourceId AND e.targetNode.id = :targetId")
    Optional<TopicEdgeEntity> findBySourceNodeIdAndTargetNodeId(@Param("sourceId") UUID sourceId, @Param("targetId") UUID targetId);

    @Query("SELECT e FROM TopicEdgeEntity e WHERE e.sourceNode.id = :nodeId OR e.targetNode.id = :nodeId")
    List<TopicEdgeEntity> findByConnectedNodeId(@Param("nodeId") UUID nodeId);

    List<TopicEdgeEntity> findAllByOrderByWeightDesc();
}
