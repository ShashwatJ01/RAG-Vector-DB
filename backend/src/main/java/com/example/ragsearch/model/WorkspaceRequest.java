package com.example.ragsearch.model;

public class WorkspaceRequest {
    private String name;
    private String description;
    private String category;
    private String knowledgeModel;
    private String chatModel;
    private String embeddingModel;
    private Integer embeddingDimension;
    private String retrievalMode;
    private String speedMode;
    private Integer topK;

    public WorkspaceRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKnowledgeModel() {
        return knowledgeModel;
    }

    public void setKnowledgeModel(String knowledgeModel) {
        this.knowledgeModel = knowledgeModel;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
        this.chatModel = chatModel;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Integer getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(Integer embeddingDimension) {
        this.embeddingDimension = embeddingDimension;
    }

    public String getRetrievalMode() {
        return retrievalMode;
    }

    public void setRetrievalMode(String retrievalMode) {
        this.retrievalMode = retrievalMode;
    }

    public String getSpeedMode() {
        return speedMode;
    }

    public void setSpeedMode(String speedMode) {
        this.speedMode = speedMode;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
