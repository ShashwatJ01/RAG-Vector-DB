package com.example.ragsearch.model;

import java.util.List;

public class DocumentChunk {
    private String id;
    private String documentId;
    private String content;
    private List<Double> embedding;

    public DocumentChunk() {
    }

    public DocumentChunk(String id, String documentId, String content, List<Double> embedding) {
        this.id = id;
        this.documentId = documentId;
        this.content = content;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
