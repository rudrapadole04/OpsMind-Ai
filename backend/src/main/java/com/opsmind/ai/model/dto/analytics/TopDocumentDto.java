package com.opsmind.ai.model.dto.analytics;

public class TopDocumentDto {

    private String filename;
    private long citationCount;
    private double citationPercentage;

    public TopDocumentDto() {
    }

    public TopDocumentDto(String filename, long citationCount, double citationPercentage) {
        this.filename = filename;
        this.citationCount = citationCount;
        this.citationPercentage = citationPercentage;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(long citationCount) {
        this.citationCount = citationCount;
    }

    public double getCitationPercentage() {
        return citationPercentage;
    }

    public void setCitationPercentage(double citationPercentage) {
        this.citationPercentage = citationPercentage;
    }
}
