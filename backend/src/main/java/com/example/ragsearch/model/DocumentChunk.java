package com.example.ragsearch.model;

import java.util.List;

public class DocumentChunk {
    private String id;
    private String documentId;
    private String fileName;
    private String content;
    private List<Double> embedding;
    private Double distance;

    public DocumentChunk() {
    }

    public DocumentChunk(String id, String documentId, String content, List<Double> embedding) {
        this(id, documentId, null, content, embedding, null);
    }

    public DocumentChunk(String id, String documentId, String fileName, String content, List<Double> embedding, Double distance) {
        this.id = id;
        this.documentId = documentId;
        this.fileName = fileName;
        this.content = content;
        this.embedding = embedding;
        this.distance = distance;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
