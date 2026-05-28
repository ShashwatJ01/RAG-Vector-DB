package com.example.ragsearch.model;

import java.time.OffsetDateTime;

public class DocumentMetadata {
    private String id;
    private String fileName;
    private long length;
    private int chunks;
    private OffsetDateTime createdAt;

    public DocumentMetadata() {
    }

    public DocumentMetadata(String id, String fileName, long length) {
        this(id, fileName, length, 0, null);
    }

    public DocumentMetadata(String id, String fileName, long length, int chunks, OffsetDateTime createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.length = length;
        this.chunks = chunks;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
