package com.example.ragsearch.model;

import java.util.List;

public class QueryResponse {
    private String answer;
    private List<SourceCitation> sources;
    private String confidence;

    public QueryResponse() {
    }

    public QueryResponse(String answer, List<SourceCitation> sources) {
        this(answer, sources, sources == null || sources.isEmpty() ? "Low" : "Grounded");
    }

    public QueryResponse(String answer, List<SourceCitation> sources, String confidence) {
        this.answer = answer;
        this.sources = sources;
        this.confidence = confidence;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<SourceCitation> getSources() {
        return sources;
    }

    public void setSources(List<SourceCitation> sources) {
        this.sources = sources;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}
