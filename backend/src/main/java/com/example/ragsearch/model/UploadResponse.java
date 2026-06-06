package com.example.ragsearch.model;

public class UploadResponse {
    private String documentId;
    private String fileName;
    private long size;
    private DocumentStatus status;
    private String jobId;
    private boolean duplicate;

    public UploadResponse() {
    }

    public UploadResponse(String documentId, String fileName, long size) {
        this(documentId, fileName, size, DocumentStatus.INDEXED, null, false);
    }

    public UploadResponse(String documentId, String fileName, long size, DocumentStatus status, String jobId, boolean duplicate) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.size = size;
        this.status = status;
        this.jobId = jobId;
        this.duplicate = duplicate;
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

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }
}
