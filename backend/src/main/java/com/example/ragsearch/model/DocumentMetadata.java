package com.example.ragsearch.model;

import java.time.OffsetDateTime;

public class DocumentMetadata {
    private String id;
    private String workspaceId;
    private String embeddingModel;
    private int embeddingDimension;
    private String fileName;
    private long length;
    private int chunks;
    private OffsetDateTime createdAt;
    private DocumentStatus status = DocumentStatus.INDEXED;
    private String fileHash;
    private String errorMessage;

    public DocumentMetadata() {
    }

    public DocumentMetadata(String id, String fileName, long length) {
        this(id, fileName, length, 0, null);
    }

    public DocumentMetadata(String id, String fileName, long length, int chunks, OffsetDateTime createdAt) {
        this(id, null, fileName, length, chunks, createdAt);
    }

    public DocumentMetadata(String id, String workspaceId, String fileName, long length, int chunks, OffsetDateTime createdAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.fileName = fileName;
        this.length = length;
        this.chunks = chunks;
        this.createdAt = createdAt;
        this.embeddingDimension = 1536;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getChunks() {
        return chunks;
    }

    public void setChunks(int chunks) {
        this.chunks = chunks;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
