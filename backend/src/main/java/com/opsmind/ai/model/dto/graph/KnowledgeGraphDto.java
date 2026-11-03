package com.opsmind.ai.model.dto.graph;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeGraphDto {

    private List<GraphNodeDto> nodes = new ArrayList<>();
    private List<GraphEdgeDto> edges = new ArrayList<>();
    private int totalNodes;
    private int totalEdges;
    private String topCategory;
    private long totalQueriesProcessed;

    public KnowledgeGraphDto() {
    }

    public KnowledgeGraphDto(List<GraphNodeDto> nodes, List<GraphEdgeDto> edges, String topCategory, long totalQueriesProcessed) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
        this.totalNodes = this.nodes.size();
        this.totalEdges = this.edges.size();
        this.topCategory = topCategory != null ? topCategory : "Enterprise Operations";
        this.totalQueriesProcessed = totalQueriesProcessed;
    }

    public List<GraphNodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNodeDto> nodes) {
        this.nodes = nodes;
        this.totalNodes = nodes != null ? nodes.size() : 0;
    }

    public List<GraphEdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdgeDto> edges) {
        this.edges = edges;
        this.totalEdges = edges != null ? edges.size() : 0;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public int getTotalEdges() {
        return totalEdges;
    }

    public void setTotalEdges(int totalEdges) {
        this.totalEdges = totalEdges;
    }

    public String getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(String topCategory) {
        this.topCategory = topCategory;
    }

    public long getTotalQueriesProcessed() {
        return totalQueriesProcessed;
    }

    public void setTotalQueriesProcessed(long totalQueriesProcessed) {
        this.totalQueriesProcessed = totalQueriesProcessed;
    }
}
