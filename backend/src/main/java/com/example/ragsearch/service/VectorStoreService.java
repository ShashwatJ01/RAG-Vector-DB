package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {
    private static final Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    private final JdbcTemplate jdbcTemplate;
    private final int vectorDimension;

    public VectorStoreService(JdbcTemplate jdbcTemplate,
                              @Value("${google.embedding.output-dimensionality}") int vectorDimension) {
        this.jdbcTemplate = jdbcTemplate;
        this.vectorDimension = vectorDimension;
    }

    public void saveDocumentMetadata(DocumentMetadata metadata) {
        logger.debug("Saving document metadata for id={} fileName={}", metadata.getId(), metadata.getFileName());
        jdbcTemplate.update(
                "INSERT INTO documents (id, file_name, length) VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
                metadata.getId(), metadata.getFileName(), metadata.getLength());
    }

    public void saveChunk(DocumentChunk chunk) {
        String embeddingVector = toVectorLiteral(chunk.getEmbedding());
        logger.debug("Saving chunk id={} documentId={} embeddingVectorLength={}", chunk.getId(), chunk.getDocumentId(), chunk.getEmbedding().size());
        jdbcTemplate.update(
                "INSERT INTO document_chunks (id, document_id, content, embedding) VALUES (?, ?, ?, ?::vector)",
                chunk.getId(), chunk.getDocumentId(), chunk.getContent(), embeddingVector);
    }

    public List<DocumentMetadata> listDocuments() {
        logger.debug("Querying all documents from vector store");
        return jdbcTemplate.query(
                "SELECT id, file_name, length FROM documents ORDER BY file_name",
                (rs, rowNum) -> new DocumentMetadata(
                        rs.getString("id"),
                        rs.getString("file_name"),
                        rs.getLong("length")
                )
        );
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit) {
        String queryVector = toVectorLiteral(queryEmbedding);
        logger.debug("Searching nearest chunks with limit={} and vector length={}", limit, queryEmbedding.size());
        return jdbcTemplate.query(
                "SELECT id, document_id, content FROM document_chunks " +
                        "ORDER BY embedding <=> ?::vector " +
                        "LIMIT ?",
                new Object[]{queryVector, limit},
                (rs, rowNum) -> new DocumentChunk(
                        rs.getString("id"),
                        rs.getString("document_id"),
                        rs.getString("content"),
                        null
                )
        );
    }

    private String toVectorLiteral(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException("Embedding cannot be null or empty");
        }
        if (embedding.size() != vectorDimension) {
            logger.warn("Embedding length {} does not match configured vector dimension {}", embedding.size(), vectorDimension);
        }
        return embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
