package com.opsmind.ai.model.dto;

public class TextChunk {
    private final int pageNumber;
    private final int chunkIndex;
    private final String content;
    private final int startChar;
    private final int endChar;

    public TextChunk(int pageNumber, int chunkIndex, String content, int startChar, int endChar) {
        this.pageNumber = pageNumber;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.startChar = startChar;
        this.endChar = endChar;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public int getStartChar() {
        return startChar;
    }

    public int getEndChar() {
        return endChar;
    }
}
