package com.example.ragsearch.model;

import java.util.List;

public class QueryRequest {
    private String query;
    private String workspaceId;
    private List<String> documentIds;
    private Integer topK;
    private Integer topN;
    private Boolean rerank = true;
    private Boolean compareReranking = false;
    private String searchMode = "semantic";
    private Double semanticWeight;
    private Double keywordWeight;

    public QueryRequest() {
    }

    public QueryRequest(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public List<String> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(List<String> documentIds) {
        this.documentIds = documentIds;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }

    public Boolean getRerank() {
        return rerank;
    }

    public void setRerank(Boolean rerank) {
        this.rerank = rerank;
    }

    public Boolean getCompareReranking() {
        return compareReranking;
    }

    public void setCompareReranking(Boolean compareReranking) {
        this.compareReranking = compareReranking;
    }

    public String getSearchMode() {
        return searchMode;
    }

    public void setSearchMode(String searchMode) {
        this.searchMode = searchMode;
    }

    public Double getSemanticWeight() {
        return semanticWeight;
    }

    public void setSemanticWeight(Double semanticWeight) {
        this.semanticWeight = semanticWeight;
    }

    public Double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(Double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }
}
