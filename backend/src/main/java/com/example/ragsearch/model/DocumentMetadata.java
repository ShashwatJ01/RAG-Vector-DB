package com.example.ragsearch.model;

public class DocumentMetadata {
    private String id;
    private String fileName;
    private long length;

    public DocumentMetadata() {
    }

    public DocumentMetadata(String id, String fileName, long length) {
        this.id = id;
        this.fileName = fileName;
        this.length = length;
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
}
