package com.example.ragsearch.model;

public enum SearchMode {
    SEMANTIC,
    KEYWORD,
    HYBRID;

    public static SearchMode from(String value) {
        if (value == null || value.isBlank()) {
            return SEMANTIC;
        }

        return switch (value.trim().toUpperCase()) {
            case "KEYWORD" -> KEYWORD;
            case "HYBRID" -> HYBRID;
            case "SEMANTIC" -> SEMANTIC;
            default -> throw new IllegalArgumentException("Unsupported search mode: " + value);
        };
    }
}
