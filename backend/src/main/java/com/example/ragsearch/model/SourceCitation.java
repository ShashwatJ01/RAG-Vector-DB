package com.example.ragsearch.model;

public class SourceCitation {
    private String id;
    private String documentId;
    private String fileName;
    private int chunkIndex;
    private Double similarityScore;
    private String content;

    public SourceCitation() {
    }

    public SourceCitation(String id, String documentId, String fileName, int chunkIndex, Double similarityScore, String content) {
        this.id = id;
        this.documentId = documentId;
        this.fileName = fileName;
        this.chunkIndex = chunkIndex;
        this.similarityScore = similarityScore;
        this.content = content;
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

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
