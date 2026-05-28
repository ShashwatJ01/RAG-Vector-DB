package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
                "SELECT d.id, d.file_name, d.length, d.uploaded_at, COUNT(c.id) AS chunks " +
                        "FROM documents d " +
                        "LEFT JOIN document_chunks c ON c.document_id = d.id " +
                        "GROUP BY d.id, d.file_name, d.length, d.uploaded_at " +
                        "ORDER BY d.uploaded_at DESC",
                (rs, rowNum) -> {
                    Timestamp createdAt = rs.getTimestamp("uploaded_at");
                    return new DocumentMetadata(
                            rs.getString("id"),
                            rs.getString("file_name"),
                            rs.getLong("length"),
                            rs.getInt("chunks"),
                            createdAt == null ? null : createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC)
                    );
                }
        );
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit) {
        return searchNearest(queryEmbedding, limit, List.of());
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit, List<String> documentIds) {
        String queryVector = toVectorLiteral(queryEmbedding);
        logger.debug("Searching nearest chunks with limit={}, vector length={}, filtered documents={}",
                limit, queryEmbedding.size(), documentIds == null ? 0 : documentIds.size());
        String documentFilter = documentIds == null || documentIds.isEmpty()
                ? ""
                : "WHERE dc.document_id IN (" + documentIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ";
        Object[] parameters = buildSearchParameters(queryVector, documentIds, limit);
        return jdbcTemplate.query(
                "SELECT dc.id, dc.document_id, d.file_name, dc.content, (dc.embedding <=> ?::vector) AS distance " +
                        "FROM document_chunks dc " +
                        "JOIN documents d ON d.id = dc.document_id " +
                        documentFilter +
                        "ORDER BY distance " +
                        "LIMIT ?",
                parameters,
                (rs, rowNum) -> new DocumentChunk(
                        rs.getString("id"),
                        rs.getString("document_id"),
                        rs.getString("file_name"),
                        rs.getString("content"),
                        null,
                        rs.getDouble("distance")
                )
        );
    }

    public void deleteDocument(String documentId) {
        logger.info("Deleting document id={} and cascaded chunks", documentId);
        jdbcTemplate.update("DELETE FROM documents WHERE id = ?", documentId);
    }

    private Object[] buildSearchParameters(String queryVector, List<String> documentIds, int limit) {
        if (documentIds == null || documentIds.isEmpty()) {
            return new Object[]{queryVector, limit};
        }
        Object[] parameters = new Object[documentIds.size() + 2];
        parameters[0] = queryVector;
        for (int i = 0; i < documentIds.size(); i++) {
            parameters[i + 1] = documentIds.get(i);
        }
        parameters[parameters.length - 1] = limit;
        return parameters;
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
