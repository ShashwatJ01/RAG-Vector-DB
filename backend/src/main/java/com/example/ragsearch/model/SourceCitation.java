package com.example.ragsearch.model;

public class SourceCitation {
    private String id;
    private String documentId;
    private String fileName;
    private int chunkIndex;
    private Double similarityScore;
    private Integer originalRank;
    private Double rerankScore;
    private Integer finalRank;
    private String content;

    public SourceCitation() {
    }

    public SourceCitation(String id, String documentId, String fileName, int chunkIndex, Double similarityScore, String content) {
        this(id, documentId, fileName, chunkIndex, similarityScore, null, null, null, content);
    }

    public SourceCitation(String id,
                          String documentId,
                          String fileName,
                          int chunkIndex,
                          Double similarityScore,
                          Integer originalRank,
                          Double rerankScore,
                          Integer finalRank,
                          String content) {
        this.id = id;
        this.documentId = documentId;
        this.fileName = fileName;
        this.chunkIndex = chunkIndex;
        this.similarityScore = similarityScore;
        this.originalRank = originalRank;
        this.rerankScore = rerankScore;
        this.finalRank = finalRank;
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

    public Integer getOriginalRank() {
        return originalRank;
    }

    public void setOriginalRank(Integer originalRank) {
        this.originalRank = originalRank;
    }

    public Double getRerankScore() {
        return rerankScore;
    }

    public void setRerankScore(Double rerankScore) {
        this.rerankScore = rerankScore;
    }

    public Integer getFinalRank() {
        return finalRank;
    }

    public void setFinalRank(Integer finalRank) {
        this.finalRank = finalRank;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
