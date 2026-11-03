package com.opsmind.ai.service.graph;

import com.opsmind.ai.model.dto.graph.GraphEdgeDto;
import com.opsmind.ai.model.dto.graph.GraphNodeDto;
import com.opsmind.ai.model.dto.graph.KnowledgeGraphDto;
import com.opsmind.ai.model.entity.CitationEntity;
import com.opsmind.ai.model.entity.DocumentChunkEntity;
import com.opsmind.ai.model.entity.DocumentEntity;
import com.opsmind.ai.model.entity.QueryLogEntity;
import com.opsmind.ai.model.entity.TopicEdgeEntity;
import com.opsmind.ai.model.entity.TopicNodeEntity;
import com.opsmind.ai.repository.DocumentChunkRepository;
import com.opsmind.ai.repository.DocumentRepository;
import com.opsmind.ai.repository.QueryLogRepository;
import com.opsmind.ai.repository.TopicEdgeRepository;
import com.opsmind.ai.repository.TopicNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeGraphService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeGraphService.class);

    private final TopicNodeRepository topicNodeRepository;
    private final TopicEdgeRepository topicEdgeRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final QueryLogRepository queryLogRepository;

    // Canonical Topic Definitions: Name -> (Category, List of search keywords)
    public record TopicDef(String name, String category, List<String> keywords) {}

    private static final List<TopicDef> CANONICAL_TOPICS = List.of(
            new TopicDef("Customer Refunds", "Finance & Billing", List.of("refund", "refunds", "credit", "reimbursement")),
            new TopicDef("Chargeback Disputes", "Finance & Billing", List.of("chargeback", "dispute", "rebuttal", "merchant")),
            new TopicDef("Fraud Prevention", "Risk & Compliance", List.of("fraud", "suspicious", "card attempts", "risk escalation")),
            new TopicDef("Extended Sick Leave", "Human Resources", List.of("sick leave", "extended sick", "medical leave", "illness")),
            new TopicDef("Doctor Certification", "Human Resources", List.of("doctor", "physician", "medical certificate", "note")),
            new TopicDef("Continuous Service Eligibility", "Human Resources", List.of("90 days", "continuous service", "eligibility", "tenure")),
            new TopicDef("VPN Remote Access", "Information Security", List.of("vpn", "remote access", "gateway", "tunneling")),
            new TopicDef("Multi-Factor Authentication", "Information Security", List.of("mfa", "2fa", "authentication", "totp", "token")),
            new TopicDef("Inactivity Auto-Disconnect", "Information Security", List.of("inactivity", "auto-disconnect", "idle", "session limit")),
            new TopicDef("Data Encryption & Security", "Information Security", List.of("encryption", "bitlocker", "endpoint", "security standard"))
    );

    // Initial Canonical Relational Connections: (SourceTopic, TargetTopic, BaseWeight, RelationType)
    public record RelationDef(String source, String target, double baseWeight, String relationType) {}

    private static final List<RelationDef> CANONICAL_RELATIONS = List.of(
            new RelationDef("Customer Refunds", "Chargeback Disputes", 2.5, "CO_OCCURRENCE"),
            new RelationDef("Customer Refunds", "Fraud Prevention", 1.8, "POLICY_DEPENDENCY"),
            new RelationDef("Chargeback Disputes", "Fraud Prevention", 2.0, "RISK_ESCALATION"),
            new RelationDef("Extended Sick Leave", "Doctor Certification", 3.0, "COMPLIANCE_REQUIREMENT"),
            new RelationDef("Extended Sick Leave", "Continuous Service Eligibility", 2.2, "ELIGIBILITY_PREREQUISITE"),
            new RelationDef("VPN Remote Access", "Multi-Factor Authentication", 3.5, "SECURITY_PREREQUISITE"),
            new RelationDef("VPN Remote Access", "Inactivity Auto-Disconnect", 2.5, "SESSION_POLICY"),
            new RelationDef("VPN Remote Access", "Data Encryption & Security", 2.0, "ENDPOINT_COMPLIANCE"),
            new RelationDef("Fraud Prevention", "Data Encryption & Security", 1.5, "SECURITY_STANDARD")
    );

    public KnowledgeGraphService(
            TopicNodeRepository topicNodeRepository,
            TopicEdgeRepository topicEdgeRepository,
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            QueryLogRepository queryLogRepository) {
        this.topicNodeRepository = topicNodeRepository;
        this.topicEdgeRepository = topicEdgeRepository;
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.queryLogRepository = queryLogRepository;
    }

    /**
     * Builds or syncs the Knowledge Graph by examining indexed SOP documents,
     * associating chunks to topic nodes, and factoring in historical employee queries.
     */
    @Transactional
    public KnowledgeGraphDto buildOrSyncGraph() {
        log.info("Synchronizing OpsMind Knowledge Graph topology...");

        // 1. Ensure all Canonical Topic Nodes exist
        Map<String, TopicNodeEntity> nodeMap = new HashMap<>();
        for (TopicDef def : CANONICAL_TOPICS) {
            TopicNodeEntity node = topicNodeRepository.findByNameIgnoreCase(def.name())
                    .orElseGet(() -> topicNodeRepository.save(new TopicNodeEntity(def.name(), def.category())));
            nodeMap.put(def.name(), node);
        }

        // 2. Compute Document Associations
        List<DocumentEntity> allDocs = documentRepository.findAll();
        for (TopicDef def : CANONICAL_TOPICS) {
            TopicNodeEntity node = nodeMap.get(def.name());
            int matchingDocCount = 0;

            for (DocumentEntity doc : allDocs) {
                boolean matched = false;
                if (doc.getChunks() != null) {
                    for (DocumentChunkEntity chunk : doc.getChunks()) {
                        String lowerContent = chunk.getContent().toLowerCase();
                        for (String kw : def.keywords()) {
                            if (lowerContent.contains(kw.toLowerCase())) {
                                matched = true;
                                break;
                            }
                        }
                        if (matched) break;
                    }
                }
                if (matched) matchingDocCount++;
            }
            node.setAssociatedDocumentCount(matchingDocCount);
        }

        // 3. Compute Query Frequencies from Query Logs
        List<QueryLogEntity> queryLogs = queryLogRepository.findAll();
        for (TopicDef def : CANONICAL_TOPICS) {
            TopicNodeEntity node = nodeMap.get(def.name());
            int hits = 0;
            for (QueryLogEntity ql : queryLogs) {
                String lowerQ = ql.getUserQuestion().toLowerCase();
                for (String kw : def.keywords()) {
                    if (lowerQ.contains(kw.toLowerCase())) {
                        hits++;
                        break;
                    }
                }
            }
            node.setQueryFrequency(hits);
            topicNodeRepository.save(node);
        }

        // 4. Ensure Canonical Edges exist and synchronize edge weights
        for (RelationDef rel : CANONICAL_RELATIONS) {
            TopicNodeEntity source = nodeMap.get(rel.source());
            TopicNodeEntity target = nodeMap.get(rel.target());

            if (source != null && target != null) {
                // Ensure deterministic ordering to honor uk_edge_source_target
                TopicNodeEntity first = source;
                TopicNodeEntity second = target;
                if (first.getId().compareTo(second.getId()) > 0) {
                    first = target;
                    second = source;
                }

                TopicNodeEntity finalFirst = first;
                TopicNodeEntity finalSecond = second;
                TopicEdgeEntity edge = topicEdgeRepository.findBySourceNodeIdAndTargetNodeId(finalFirst.getId(), finalSecond.getId())
                        .orElseGet(() -> new TopicEdgeEntity(finalFirst, finalSecond, rel.baseWeight(), rel.relationType()));

                // Boost edge weight dynamically based on combined query frequencies of connected topics
                double trafficBoost = (source.getQueryFrequency() + target.getQueryFrequency()) * 0.2;
                edge.setWeight(Math.round((rel.baseWeight() + trafficBoost) * 100.0) / 100.0);
                edge.setRelationshipType(rel.relationType());
                topicEdgeRepository.save(edge);
            }
        }

        log.info("Knowledge Graph synced: {} nodes, {} edges", nodeMap.size(), CANONICAL_RELATIONS.size());
        return getKnowledgeGraph();
    }

    /**
     * Records query topic hits in real-time, incrementing topic query frequencies
     * and boosting weights of connecting edges.
     */
    @Transactional
    public void recordQueryTopicHits(String question, List<CitationEntity> citations) {
        if (question == null || question.isBlank()) {
            return;
        }

        String lowerQuestion = question.toLowerCase();
        List<TopicNodeEntity> matchedNodes = new ArrayList<>();

        for (TopicDef def : CANONICAL_TOPICS) {
            boolean matched = false;
            for (String kw : def.keywords()) {
                if (lowerQuestion.contains(kw.toLowerCase())) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                topicNodeRepository.findByNameIgnoreCase(def.name()).ifPresent(node -> {
                    node.incrementQueryFrequency();
                    topicNodeRepository.save(node);
                    matchedNodes.add(node);
                });
            }
        }

        // If multiple topics matched the question, boost connecting edge weight
        if (matchedNodes.size() >= 2) {
            for (int i = 0; i < matchedNodes.size(); i++) {
                for (int j = i + 1; j < matchedNodes.size(); j++) {
                    TopicNodeEntity a = matchedNodes.get(i);
                    TopicNodeEntity b = matchedNodes.get(j);
                    TopicNodeEntity source = a.getId().compareTo(b.getId()) < 0 ? a : b;
                    TopicNodeEntity target = a.getId().compareTo(b.getId()) < 0 ? b : a;

                    topicEdgeRepository.findBySourceNodeIdAndTargetNodeId(source.getId(), target.getId())
                            .ifPresent(edge -> {
                                edge.incrementWeight(0.5);
                                topicEdgeRepository.save(edge);
                            });
                }
            }
        }
    }

    /**
     * Retrieves the entire Knowledge Graph representation for visualization.
     */
    @Transactional(readOnly = true)
    public KnowledgeGraphDto getKnowledgeGraph() {
        List<TopicNodeEntity> nodes = topicNodeRepository.findAllByOrderByQueryFrequencyDesc();
        List<TopicEdgeEntity> edges = topicEdgeRepository.findAllByOrderByWeightDesc();

        if (nodes.isEmpty()) {
            // Self-bootstrap if not yet seeded
            return buildOrSyncGraph();
        }

        List<GraphNodeDto> nodeDtos = nodes.stream()
                .map(GraphNodeDto::new)
                .collect(Collectors.toList());

        List<GraphEdgeDto> edgeDtos = edges.stream()
                .map(GraphEdgeDto::new)
                .collect(Collectors.toList());

        // Find top category by sum of query frequencies
        Map<String, Integer> categoryTotals = nodes.stream()
                .collect(Collectors.groupingBy(
                        TopicNodeEntity::getCategory,
                        Collectors.summingInt(n -> n.getQueryFrequency() != null ? n.getQueryFrequency() : 0)
                ));

        String topCategory = categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Enterprise Operations");

        long totalQueries = queryLogRepository.countTotalQueries();

        return new KnowledgeGraphDto(nodeDtos, edgeDtos, topCategory, totalQueries);
    }

    /**
     * Retrieves the top N most queried topics for quick dashboard widgets.
     */
    @Transactional(readOnly = true)
    public List<GraphNodeDto> getTopTopics(int limit) {
        return topicNodeRepository.findTop15ByOrderByQueryFrequencyDesc().stream()
                .limit(limit)
                .map(GraphNodeDto::new)
                .collect(Collectors.toList());
    }
}
