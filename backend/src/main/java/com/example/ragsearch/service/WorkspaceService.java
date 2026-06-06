package com.example.ragsearch.service;

import com.example.ragsearch.model.Workspace;
import com.example.ragsearch.model.WorkspaceRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final JdbcTemplate jdbcTemplate;

    public WorkspaceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Workspace> listWorkspaces() {
        return jdbcTemplate.query(
                "SELECT w.id, w.name, w.description, w.category, COALESCE(w.status, 'Active') AS status, " +
                        "COALESCE(w.knowledge_model, 'gemini-flash-lite') AS knowledge_model, " +
                        "COALESCE(w.chat_model, 'gemini-2.5-flash-lite') AS chat_model, " +
                        "COALESCE(w.embedding_model, 'gemini-embedding-001') AS embedding_model, " +
                        "COALESCE(w.embedding_dimension, 1536) AS embedding_dimension, " +
                        "COALESCE(w.retrieval_mode, 'balanced') AS retrieval_mode, " +
                        "COALESCE(w.speed_mode, 'standard') AS speed_mode, " +
                        "COALESCE(w.top_k, 4) AS top_k, w.created_at, " +
                        "COUNT(d.id) AS document_count, MAX(d.uploaded_at) AS last_activity_at " +
                        "FROM workspaces w " +
                        "LEFT JOIN documents d ON d.workspace_id = w.id " +
                        "WHERE COALESCE(w.status, 'Active') <> 'Archived' " +
                        "GROUP BY w.id, w.name, w.description, w.category, w.status, w.knowledge_model, w.chat_model, " +
                        "w.embedding_model, w.embedding_dimension, w.retrieval_mode, w.speed_mode, w.top_k, w.created_at " +
                        "ORDER BY COALESCE(MAX(d.uploaded_at), w.created_at) DESC",
                (rs, rowNum) -> new Workspace(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("knowledge_model"),
                        rs.getString("chat_model"),
                        rs.getString("embedding_model"),
                        rs.getInt("embedding_dimension"),
                        rs.getString("retrieval_mode"),
                        rs.getString("speed_mode"),
                        rs.getInt("top_k"),
                        rs.getInt("document_count"),
                        toOffset(rs.getTimestamp("created_at")),
                        toOffset(rs.getTimestamp("last_activity_at"))
                )
        );
    }

    public Workspace createWorkspace(WorkspaceRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Workspace name is required.");
        }

        String id = UUID.randomUUID().toString();
        String description = request.getDescription() == null || request.getDescription().isBlank()
                ? "A document workspace ready for AI-powered search and analysis."
                : request.getDescription().trim();
        String category = request.getCategory() == null || request.getCategory().isBlank()
                ? "General"
                : request.getCategory().trim();
        String knowledgeModel = normalizeKnowledgeModel(request.getKnowledgeModel());
        String chatModel = normalizeChatModel(request.getChatModel(), knowledgeModel);
        String embeddingModel = normalizeEmbeddingModel(request.getEmbeddingModel());
        int embeddingDimension = request.getEmbeddingDimension() == null ? 1536 : request.getEmbeddingDimension();
        String retrievalMode = normalizeRetrievalMode(request.getRetrievalMode());
        String speedMode = normalizeSpeedMode(request.getSpeedMode());
        int topK = request.getTopK() == null ? topKForRetrievalMode(retrievalMode) : request.getTopK();

        return jdbcTemplate.queryForObject(
                "INSERT INTO workspaces (id, name, description, category, status, knowledge_model, chat_model, embedding_model, embedding_dimension, retrieval_mode, speed_mode, top_k) " +
                        "VALUES (?, ?, ?, ?, 'Active', ?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING id, name, description, category, status, knowledge_model, chat_model, embedding_model, embedding_dimension, retrieval_mode, speed_mode, top_k, created_at, 0 AS document_count, NULL AS last_activity_at",
                (rs, rowNum) -> new Workspace(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("knowledge_model"),
                        rs.getString("chat_model"),
                        rs.getString("embedding_model"),
                        rs.getInt("embedding_dimension"),
                        rs.getString("retrieval_mode"),
                        rs.getString("speed_mode"),
                        rs.getInt("top_k"),
                        rs.getInt("document_count"),
                        toOffset(rs.getTimestamp("created_at")),
                        toOffset(rs.getTimestamp("last_activity_at"))
                ),
                id,
                request.getName().trim(),
                description,
                category,
                knowledgeModel,
                chatModel,
                embeddingModel,
                embeddingDimension,
                retrievalMode,
                speedMode,
                topK
        );
    }

    public Workspace getWorkspace(String workspaceId) {
        if (workspaceId == null || workspaceId.isBlank()) {
            return null;
        }

        return jdbcTemplate.query(
                "SELECT id, name, description, category, COALESCE(status, 'Active') AS status, " +
                        "COALESCE(knowledge_model, 'gemini-flash-lite') AS knowledge_model, " +
                        "COALESCE(chat_model, 'gemini-2.5-flash-lite') AS chat_model, " +
                        "COALESCE(embedding_model, 'gemini-embedding-001') AS embedding_model, " +
                        "COALESCE(embedding_dimension, 1536) AS embedding_dimension, " +
                        "COALESCE(retrieval_mode, 'balanced') AS retrieval_mode, " +
                        "COALESCE(speed_mode, 'standard') AS speed_mode, " +
                        "COALESCE(top_k, 4) AS top_k, created_at, NULL AS last_activity_at " +
                        "FROM workspaces WHERE id = ?",
                (rs, rowNum) -> new Workspace(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("knowledge_model"),
                        rs.getString("chat_model"),
                        rs.getString("embedding_model"),
                        rs.getInt("embedding_dimension"),
                        rs.getString("retrieval_mode"),
                        rs.getString("speed_mode"),
                        rs.getInt("top_k"),
                        0,
                        toOffset(rs.getTimestamp("created_at")),
                        toOffset(rs.getTimestamp("last_activity_at"))
                ),
                workspaceId
        ).stream().findFirst().orElse(null);
    }

    public void archiveWorkspace(String workspaceId) {
        jdbcTemplate.update("UPDATE workspaces SET status = 'Archived', archived_at = now() WHERE id = ?", workspaceId);
    }

    private java.time.OffsetDateTime toOffset(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }

    private String normalizeEmbeddingModel(String embeddingModel) {
        if ("gemini-embedding-2".equals(embeddingModel)) {
            return embeddingModel;
        }
        return "gemini-embedding-001";
    }

    private String normalizeKnowledgeModel(String knowledgeModel) {
        if ("gemini-flash".equals(knowledgeModel)) {
            return knowledgeModel;
        }
        return "gemini-flash-lite";
    }

    private String normalizeChatModel(String chatModel, String knowledgeModel) {
        if (chatModel != null && !chatModel.isBlank()) {
            return chatModel.trim();
        }
        if ("gemini-flash".equals(knowledgeModel)) {
            return "gemini-2.5-flash";
        }
        return "gemini-2.5-flash-lite";
    }

    private String normalizeRetrievalMode(String retrievalMode) {
        if ("fast".equals(retrievalMode) || "high".equals(retrievalMode) || "extra-high".equals(retrievalMode)) {
            return retrievalMode;
        }
        return "balanced";
    }

    private String normalizeSpeedMode(String speedMode) {
        if ("faster".equals(speedMode)) {
            return speedMode;
        }
        return "standard";
    }

    private int topKForRetrievalMode(String retrievalMode) {
        return switch (retrievalMode) {
            case "fast" -> 3;
            case "high" -> 8;
            case "extra-high" -> 10;
            default -> 4;
        };
    }
}
