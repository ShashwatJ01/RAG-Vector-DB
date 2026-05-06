package com.example.ragsearch.model;

public class UploadResponse {
    private String documentId;
    private String fileName;
    private long size;

    public UploadResponse() {
    }

    public UploadResponse(String documentId, String fileName, long size) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.size = size;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
