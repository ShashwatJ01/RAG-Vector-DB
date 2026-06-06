package com.example.ragsearch.model;

import java.time.OffsetDateTime;

public class IngestionJob {
    private String id;
    private String documentId;
    private String workspaceId;
    private String fileName;
    private String fileHash;
    private String contentType;
    private long sourceSize;
    private byte[] payload;
    private DocumentStatus status;
    private int retryCount;
    private int maxRetries;
    private String errorMessage;
    private OffsetDateTime availableAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;

    public IngestionJob() {
    }

    public IngestionJob(String id, String documentId, String workspaceId, String fileName, String fileHash,
                        String contentType, long sourceSize, byte[] payload, DocumentStatus status,
                        int retryCount, int maxRetries, String errorMessage, OffsetDateTime availableAt,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime startedAt,
                        OffsetDateTime finishedAt) {
        this.id = id;
        this.documentId = documentId;
        this.workspaceId = workspaceId;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.contentType = contentType;
        this.sourceSize = sourceSize;
        this.payload = payload;
        this.status = status;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.errorMessage = errorMessage;
        this.availableAt = availableAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
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

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(OffsetDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }
}
