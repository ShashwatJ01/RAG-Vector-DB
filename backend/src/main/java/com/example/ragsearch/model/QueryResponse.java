package com.example.ragsearch.model;

import java.util.List;

public class QueryResponse {
    private String answer;
    private List<SourceCitation> sources;
    private String confidence;
    private Boolean reranked;
    private Integer retrievalTopN;
    private Integer finalTopK;
    private RerankComparison rerankComparison;

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

    public QueryResponse(String answer,
                         List<SourceCitation> sources,
                         String confidence,
                         Boolean reranked,
                         Integer retrievalTopN,
                         Integer finalTopK,
                         RerankComparison rerankComparison) {
        this.answer = answer;
        this.sources = sources;
        this.confidence = confidence;
        this.reranked = reranked;
        this.retrievalTopN = retrievalTopN;
        this.finalTopK = finalTopK;
        this.rerankComparison = rerankComparison;
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

    public Boolean getReranked() {
        return reranked;
    }

    public void setReranked(Boolean reranked) {
        this.reranked = reranked;
    }

    public Integer getRetrievalTopN() {
        return retrievalTopN;
    }

    public void setRetrievalTopN(Integer retrievalTopN) {
        this.retrievalTopN = retrievalTopN;
    }

    public Integer getFinalTopK() {
        return finalTopK;
    }

    public void setFinalTopK(Integer finalTopK) {
        this.finalTopK = finalTopK;
    }

    public RerankComparison getRerankComparison() {
        return rerankComparison;
    }

    public void setRerankComparison(RerankComparison rerankComparison) {
        this.rerankComparison = rerankComparison;
    }
}
