package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.DocumentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                "INSERT INTO documents (id, workspace_id, file_name, length, embedding_model, embedding_dimension, document_status, file_hash, error_message) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "workspace_id = EXCLUDED.workspace_id, file_name = EXCLUDED.file_name, length = EXCLUDED.length, " +
                        "embedding_model = EXCLUDED.embedding_model, embedding_dimension = EXCLUDED.embedding_dimension, " +
                        "document_status = EXCLUDED.document_status, file_hash = EXCLUDED.file_hash, error_message = EXCLUDED.error_message, updated_at = now()",
                metadata.getId(), metadata.getWorkspaceId(), metadata.getFileName(), metadata.getLength(),
                metadata.getEmbeddingModel(), metadata.getEmbeddingDimension(), metadata.getStatus().name(),
                metadata.getFileHash(), metadata.getErrorMessage());
    }

    public void saveChunk(DocumentChunk chunk) {
        String embeddingVector = toVectorLiteral(chunk.getEmbedding());
        logger.debug("Saving chunk id={} documentId={} embeddingVectorLength={}", chunk.getId(), chunk.getDocumentId(), chunk.getEmbedding().size());
        jdbcTemplate.update(
                "INSERT INTO document_chunks (id, document_id, content, embedding) VALUES (?, ?, ?, ?::vector)",
                chunk.getId(), chunk.getDocumentId(), chunk.getContent(), embeddingVector);
    }

    public List<DocumentMetadata> listDocuments() {
        return listDocuments(null);
    }

    public List<DocumentMetadata> listDocuments(String workspaceId) {
        logger.debug("Querying all documents from vector store");
        String workspaceFilter = workspaceId == null || workspaceId.isBlank() ? "" : "WHERE d.workspace_id = ? ";
        Object[] parameters = workspaceId == null || workspaceId.isBlank() ? new Object[]{} : new Object[]{workspaceId};
        return jdbcTemplate.query(
                "SELECT d.id, d.workspace_id, d.embedding_model, d.embedding_dimension, d.file_name, d.length, d.uploaded_at, " +
                        "COALESCE(d.document_status, 'INDEXED') AS document_status, d.file_hash, d.error_message, COUNT(c.id) AS chunks " +
                        "FROM documents d " +
                        "LEFT JOIN document_chunks c ON c.document_id = d.id " +
                        workspaceFilter +
                        "GROUP BY d.id, d.workspace_id, d.embedding_model, d.embedding_dimension, d.file_name, d.length, d.uploaded_at, d.document_status, d.file_hash, d.error_message " +
                        "ORDER BY d.uploaded_at DESC",
                parameters,
                (rs, rowNum) -> mapDocumentMetadata(rs)
        );
    }

    public Optional<DocumentMetadata> findDocumentByFileHash(String workspaceId, String fileHash) {
        if (fileHash == null || fileHash.isBlank()) {
            return Optional.empty();
        }

        return jdbcTemplate.query(
                "SELECT d.id, d.workspace_id, d.embedding_model, d.embedding_dimension, d.file_name, d.length, d.uploaded_at, " +
                        "COALESCE(d.document_status, 'INDEXED') AS document_status, d.file_hash, d.error_message, COUNT(c.id) AS chunks " +
                        "FROM documents d " +
                        "LEFT JOIN document_chunks c ON c.document_id = d.id " +
                        "WHERE d.file_hash = ? AND ((? IS NULL AND d.workspace_id IS NULL) OR d.workspace_id = ?) " +
                        "GROUP BY d.id, d.workspace_id, d.embedding_model, d.embedding_dimension, d.file_name, d.length, d.uploaded_at, d.document_status, d.file_hash, d.error_message " +
                        "ORDER BY d.uploaded_at DESC LIMIT 1",
                (rs, rowNum) -> mapDocumentMetadata(rs),
                fileHash,
                normalizeWorkspaceId(workspaceId),
                normalizeWorkspaceId(workspaceId)
        ).stream().findFirst();
    }

    public void updateDocumentStatus(String documentId, DocumentStatus status, String errorMessage) {
        jdbcTemplate.update(
                "UPDATE documents SET document_status = ?, error_message = ?, updated_at = now() WHERE id = ?",
                status.name(),
                errorMessage,
                documentId
        );
    }

    public void markDocumentIndexed(String documentId, long textLength, String embeddingModel, int embeddingDimension) {
        jdbcTemplate.update(
                "UPDATE documents SET document_status = 'INDEXED', length = ?, embedding_model = ?, embedding_dimension = ?, error_message = NULL, indexed_at = now(), updated_at = now() WHERE id = ?",
                textLength,
                embeddingModel,
                embeddingDimension,
                documentId
        );
    }

    public void deleteDocumentChunks(String documentId) {
        jdbcTemplate.update("DELETE FROM document_chunks WHERE document_id = ?", documentId);
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit) {
        return searchNearest(queryEmbedding, limit, List.of());
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit, List<String> documentIds) {
        return searchNearest(queryEmbedding, limit, null, documentIds);
    }

    public List<DocumentChunk> searchNearest(List<Double> queryEmbedding, int limit, String workspaceId, List<String> documentIds) {
        long startedAt = System.currentTimeMillis();
        String queryVector = toVectorLiteral(queryEmbedding);
        boolean hasDocumentFilter = documentIds != null && !documentIds.isEmpty();
        boolean hasWorkspaceFilter = workspaceId != null && !workspaceId.isBlank();
        logger.info("vector search started mode=semantic limit={} vectorDimension={} workspaceFiltered={} documentCount={}",
                limit,
                queryEmbedding.size(),
                hasWorkspaceFilter,
                documentFilterCount(documentIds));
        String documentFilter = "";
        if (hasDocumentFilter) {
            documentFilter = "WHERE d.document_status = 'INDEXED' AND dc.document_id IN (" + documentIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ";
        } else if (hasWorkspaceFilter) {
            documentFilter = "WHERE d.document_status = 'INDEXED' AND d.workspace_id = ? ";
        } else {
            documentFilter = "WHERE d.document_status = 'INDEXED' ";
        }
        Object[] parameters = buildSearchParameters(queryVector, workspaceId, documentIds, limit);
        List<DocumentChunk> results = jdbcTemplate.query(
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
        logger.info("vector search completed mode=semantic resultCount={} limit={} durationMs={}",
                results.size(),
                limit,
                System.currentTimeMillis() - startedAt);
        return results;
    }

    public List<DocumentChunk> searchKeyword(String queryText, int limit, String workspaceId, List<String> documentIds) {
        long startedAt = System.currentTimeMillis();
        boolean hasDocumentFilter = documentIds != null && !documentIds.isEmpty();
        boolean hasWorkspaceFilter = !hasDocumentFilter && workspaceId != null && !workspaceId.isBlank();
        logger.info("vector search started mode=keyword limit={} workspaceFiltered={} documentCount={} queryLength={}",
                limit,
                hasWorkspaceFilter,
                documentFilterCount(documentIds),
                queryText == null ? 0 : queryText.length());
        String documentFilter = "WHERE d.document_status = 'INDEXED' AND dc.content_search @@ q.query ";
        if (hasDocumentFilter) {
            documentFilter += "AND dc.document_id IN (" + documentIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ";
        } else if (hasWorkspaceFilter) {
            documentFilter += "AND d.workspace_id = ? ";
        }

        List<Object> parameters = new ArrayList<>();
        parameters.add(queryText);
        if (hasDocumentFilter) {
            parameters.addAll(documentIds);
        } else if (hasWorkspaceFilter) {
            parameters.add(workspaceId);
        }
        parameters.add(limit);

        List<DocumentChunk> results = jdbcTemplate.query(
                "WITH q AS (SELECT websearch_to_tsquery('english', ?) AS query) " +
                        "SELECT dc.id, dc.document_id, d.file_name, dc.content, " +
                        "GREATEST(0.0, 1.0 - LEAST(ts_rank_cd(dc.content_search, q.query, 32)::double precision, 1.0)) AS distance " +
                        "FROM document_chunks dc " +
                        "JOIN documents d ON d.id = dc.document_id " +
                        "CROSS JOIN q " +
                        documentFilter +
                        "ORDER BY ts_rank_cd(dc.content_search, q.query, 32) DESC " +
                        "LIMIT ?",
                parameters.toArray(),
                (rs, rowNum) -> mapChunkSearchResult(rs)
        );
        logger.info("vector search completed mode=keyword resultCount={} limit={} durationMs={}",
                results.size(),
                limit,
                System.currentTimeMillis() - startedAt);
        return results;
    }

    public List<DocumentChunk> searchHybrid(String queryText,
                                            List<Double> queryEmbedding,
                                            int limit,
                                            String workspaceId,
                                            List<String> documentIds,
                                            double semanticWeight,
                                            double keywordWeight) {
        long startedAt = System.currentTimeMillis();
        String queryVector = toVectorLiteral(queryEmbedding);
        List<String> filteredDocumentIds = documentIds == null ? List.of() : documentIds;
        String workspaceFilter = filteredDocumentIds.isEmpty() ? normalizeWorkspaceId(workspaceId) : null;
        logger.info("vector search started mode=hybrid limit={} vectorDimension={} workspaceFiltered={} documentCount={} semanticWeight={} keywordWeight={} queryLength={}",
                limit,
                queryEmbedding.size(),
                workspaceFilter != null,
                filteredDocumentIds.size(),
                semanticWeight,
                keywordWeight,
                queryText == null ? 0 : queryText.length());

        List<DocumentChunk> results = jdbcTemplate.query(
                "SELECT id, document_id, file_name, content, GREATEST(0.0, 1.0 - relevance_score) AS distance " +
                        "FROM public.hybrid_search_document_chunks(?, ?::vector, ?, ?, ?::text[], ?, ?, 50)",
                statement -> {
                    statement.setString(1, queryText);
                    statement.setString(2, queryVector);
                    statement.setInt(3, limit);
                    if (workspaceFilter == null) {
                        statement.setNull(4, Types.VARCHAR);
                    } else {
                        statement.setString(4, workspaceFilter);
                    }
                    Array documentArray = statement.getConnection().createArrayOf("text", filteredDocumentIds.toArray(new String[0]));
                    statement.setArray(5, documentArray);
                    statement.setDouble(6, semanticWeight);
                    statement.setDouble(7, keywordWeight);
                },
                (rs, rowNum) -> mapChunkSearchResult(rs)
        );
        logger.info("vector search completed mode=hybrid resultCount={} limit={} durationMs={}",
                results.size(),
                limit,
                System.currentTimeMillis() - startedAt);
        return results;
    }

    public void deleteDocument(String documentId) {
        logger.info("Deleting document id={} and cascaded chunks", documentId);
        jdbcTemplate.update("DELETE FROM documents WHERE id = ?", documentId);
    }

    private Object[] buildSearchParameters(String queryVector, List<String> documentIds, int limit) {
        return buildSearchParameters(queryVector, null, documentIds, limit);
    }

    private Object[] buildSearchParameters(String queryVector, String workspaceId, List<String> documentIds, int limit) {
        if (documentIds == null || documentIds.isEmpty()) {
            if (workspaceId != null && !workspaceId.isBlank()) {
                return new Object[]{queryVector, workspaceId, limit};
            }
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

    private int documentFilterCount(List<String> documentIds) {
        return documentIds == null ? 0 : documentIds.size();
    }

    private DocumentChunk mapChunkSearchResult(ResultSet rs) throws SQLException {
        return new DocumentChunk(
                rs.getString("id"),
                rs.getString("document_id"),
                rs.getString("file_name"),
                rs.getString("content"),
                null,
                rs.getDouble("distance")
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

    private DocumentMetadata mapDocumentMetadata(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("uploaded_at");
        DocumentMetadata metadata = new DocumentMetadata(
                rs.getString("id"),
                rs.getString("workspace_id"),
                rs.getString("file_name"),
                rs.getLong("length"),
                rs.getInt("chunks"),
                createdAt == null ? null : createdAt.toInstant().atOffset(java.time.ZoneOffset.UTC)
        );
        metadata.setEmbeddingModel(rs.getString("embedding_model"));
        metadata.setEmbeddingDimension(rs.getInt("embedding_dimension"));
        metadata.setStatus(DocumentStatus.valueOf(rs.getString("document_status")));
        metadata.setFileHash(rs.getString("file_hash"));
        metadata.setErrorMessage(rs.getString("error_message"));
        return metadata;
    }

    private String normalizeWorkspaceId(String workspaceId) {
        return workspaceId == null || workspaceId.isBlank() ? null : workspaceId;
    }
}
