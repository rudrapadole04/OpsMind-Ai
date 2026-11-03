package com.opsmind.ai.repository;

import com.opsmind.ai.model.entity.TopicNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicNodeRepository extends JpaRepository<TopicNodeEntity, UUID> {

    Optional<TopicNodeEntity> findByNameIgnoreCase(String name);

    List<TopicNodeEntity> findTop15ByOrderByQueryFrequencyDesc();

    List<TopicNodeEntity> findAllByOrderByQueryFrequencyDesc();

    List<TopicNodeEntity> findByCategory(String category);
}
