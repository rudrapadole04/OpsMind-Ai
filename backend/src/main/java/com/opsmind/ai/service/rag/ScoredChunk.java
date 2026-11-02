package com.opsmind.ai.service.rag;

import com.opsmind.ai.model.entity.DocumentChunkEntity;

public class ScoredChunk implements Comparable<ScoredChunk> {
    private final DocumentChunkEntity chunk;
    private final double score;
    private final String mostRelevantSnippet;

    public ScoredChunk(DocumentChunkEntity chunk, double score, String mostRelevantSnippet) {
        this.chunk = chunk;
        this.score = score;
        this.mostRelevantSnippet = mostRelevantSnippet;
    }

    public DocumentChunkEntity getChunk() {
        return chunk;
    }

    public double getScore() {
        return score;
    }

    public String getMostRelevantSnippet() {
        return mostRelevantSnippet;
    }

    @Override
    public int compareTo(ScoredChunk o) {
        return Double.compare(o.score, this.score); // Descending order
    }
}
