package com.opsmind.ai.model.dto;

import jakarta.validation.constraints.NotBlank;

public class QueryRequest {

    @NotBlank(message = "Question cannot be blank")
    private String question;

    public QueryRequest() {
    }

    public QueryRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
