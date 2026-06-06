package com.example.ragsearch.model;

import java.time.OffsetDateTime;

public class Workspace {
    private String id;
    private String name;
    private String description;
    private String category;
    private String status;
    private String knowledgeModel;
    private String chatModel;
    private String embeddingModel;
    private int embeddingDimension;
    private String retrievalMode;
    private String speedMode;
    private int topK;
    private int documentCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastActivityAt;

    public Workspace() {
    }

    public Workspace(String id, String name, String description, String category, String status,
                     int documentCount, OffsetDateTime createdAt, OffsetDateTime lastActivityAt) {
        this(id, name, description, category, status, "gemini-flash-lite", "gemini-2.5-flash-lite",
                "gemini-embedding-001", 1536, "balanced", "standard", 4,
                documentCount, createdAt, lastActivityAt);
    }

    public Workspace(String id, String name, String description, String category, String status,
                     String embeddingModel, int embeddingDimension, int documentCount,
                     OffsetDateTime createdAt, OffsetDateTime lastActivityAt) {
        this(id, name, description, category, status, "gemini-flash-lite", "gemini-2.5-flash-lite",
                embeddingModel, embeddingDimension, "balanced", "standard", 4,
                documentCount, createdAt, lastActivityAt);
    }

    public Workspace(String id, String name, String description, String category, String status,
                     String knowledgeModel, String chatModel, String embeddingModel, int embeddingDimension,
                     String retrievalMode, String speedMode, int topK, int documentCount,
                     OffsetDateTime createdAt, OffsetDateTime lastActivityAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.status = status;
        this.knowledgeModel = knowledgeModel;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingDimension = embeddingDimension;
        this.retrievalMode = retrievalMode;
        this.speedMode = speedMode;
        this.topK = topK;
        this.documentCount = documentCount;
        this.createdAt = createdAt;
        this.lastActivityAt = lastActivityAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getEmbeddingDimension() {
        return embeddingDimension;
    }

    public void setEmbeddingDimension(int embeddingDimension) {
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

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(OffsetDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}
