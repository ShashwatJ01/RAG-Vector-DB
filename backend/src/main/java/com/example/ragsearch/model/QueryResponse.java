package com.example.ragsearch.model;

import java.util.List;

public class QueryResponse {
    private String answer;
    private List<String> sources;

    public QueryResponse() {
    }

    public QueryResponse(String answer, List<String> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }
}
