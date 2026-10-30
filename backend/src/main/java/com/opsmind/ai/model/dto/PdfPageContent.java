package com.opsmind.ai.model.dto;

public class PdfPageContent {
    private final int pageNumber;
    private final String content;

    public PdfPageContent(int pageNumber, String content) {
        this.pageNumber = pageNumber;
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getContent() {
        return content;
    }
}
