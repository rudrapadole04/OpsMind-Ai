package com.opsmind.ai.model.dto.analytics;

public class TopicMetricDto {

    private String topicName;
    private String category;
    private int queryCount;
    private int documentCount;
    private double percentage;

    public TopicMetricDto() {
    }

    public TopicMetricDto(String topicName, String category, int queryCount, int documentCount, double percentage) {
        this.topicName = topicName;
        this.category = category;
        this.queryCount = queryCount;
        this.documentCount = documentCount;
        this.percentage = percentage;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
