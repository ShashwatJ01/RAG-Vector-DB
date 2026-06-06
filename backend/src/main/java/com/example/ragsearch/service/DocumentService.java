package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.DocumentStatus;
import com.example.ragsearch.model.DocumentStatusResponse;
import com.example.ragsearch.model.IngestionJob;
import com.example.ragsearch.model.QueryResponse;
import com.example.ragsearch.model.SourceCitation;
import com.example.ragsearch.model.UploadResponse;
import com.example.ragsearch.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final GoogleGenerativeAiService googleGenerativeAiService;
    private final VectorStoreService vectorStoreService;
    private final WorkspaceService workspaceService;
    private final IngestionJobRepository ingestionJobRepository;
    private final int maxRetries;

    public DocumentService(GoogleGenerativeAiService googleGenerativeAiService,
                           VectorStoreService vectorStoreService,
                           WorkspaceService workspaceService,
                           IngestionJobRepository ingestionJobRepository,
                           @Value("${ingestion.worker.max-retries:3}") int maxRetries) {
        this.googleGenerativeAiService = googleGenerativeAiService;
        this.vectorStoreService = vectorStoreService;
        this.workspaceService = workspaceService;
        this.ingestionJobRepository = ingestionJobRepository;
        this.maxRetries = maxRetries;
        logger.info("DocumentService initialized");
    }

    public List<DocumentMetadata> listDocuments() {
        return listDocuments(null);
    }

    public List<DocumentMetadata> listDocuments(String workspaceId) {
        logger.debug("Listing documents from vector store");
        return vectorStoreService.listDocuments(workspaceId);
    }

    public UploadResponse ingestFile(MultipartFile file) throws IOException {
        return ingestFile(file, null);
    }

    public UploadResponse ingestFile(MultipartFile file, String workspaceId) throws IOException {
        logger.info("Queueing file ingestion: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }

        String normalizedWorkspaceId = normalizeWorkspaceId(workspaceId);
        byte[] payload = file.getBytes();
        String fileHash = sha256Hex(payload);
        String fileName = normalizeFileName(file.getOriginalFilename());
        Optional<DocumentMetadata> existingDocument = vectorStoreService.findDocumentByFileHash(normalizedWorkspaceId, fileHash);

        if (existingDocument.isPresent() && existingDocument.get().getStatus() != DocumentStatus.FAILED) {
            DocumentMetadata existing = existingDocument.get();
            Optional<IngestionJob> existingJob = ingestionJobRepository.findLatestByDocumentId(existing.getId());
            logger.info("Duplicate upload detected for hash={} documentId={} status={}", fileHash, existing.getId(), existing.getStatus());
            return new UploadResponse(
                    existing.getId(),
                    existing.getFileName(),
                    file.getSize(),
                    existing.getStatus(),
                    existingJob.map(IngestionJob::getId).orElse(null),
                    true
            );
        }

        String documentId = existingDocument.map(DocumentMetadata::getId).orElseGet(() -> documentIdForHash(normalizedWorkspaceId, fileHash));
        Workspace workspace = workspaceService.getWorkspace(normalizedWorkspaceId);
        String embeddingModel = workspace == null ? null : workspace.getEmbeddingModel();
        DocumentMetadata metadata = new DocumentMetadata(documentId, normalizedWorkspaceId, fileName, file.getSize(), 0, null);
        metadata.setEmbeddingModel(embeddingModel);
        metadata.setEmbeddingDimension(workspace == null ? 1536 : workspace.getEmbeddingDimension());
        metadata.setStatus(DocumentStatus.QUEUED);
        metadata.setFileHash(fileHash);
        vectorStoreService.saveDocumentMetadata(metadata);

        String jobId = UUID.randomUUID().toString();
        IngestionJob job = new IngestionJob(
                jobId,
                documentId,
                normalizedWorkspaceId,
                fileName,
                fileHash,
                file.getContentType(),
                file.getSize(),
                payload,
                DocumentStatus.QUEUED,
                0,
                maxRetries,
                null,
                OffsetDateTime.now(ZoneOffset.UTC),
                null,
                null,
                null,
                null
        );
        jobId = ingestionJobRepository.createJobIfAbsent(job);
        logger.info("Queued ingestion job={} documentId={} hash={}", jobId, documentId, fileHash);

        return new UploadResponse(documentId, fileName, file.getSize(), DocumentStatus.QUEUED, jobId, existingDocument.isPresent());
    }

    public DocumentStatusResponse getDocumentStatus(String documentId) {
        return ingestionJobRepository.getDocumentStatus(documentId);
    }

    public QueryResponse answerQuery(String query) {
        return answerQuery(query, List.of(), 4);
    }

    public QueryResponse answerQuery(String query, List<String> documentIds, int topK) {
        return answerQuery(query, null, documentIds, topK);
    }

    public QueryResponse answerQuery(String query, String workspaceId, List<String> documentIds, int topK) {
        logger.info("Processing query: '{}'", query);
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        String embeddingModel = workspace == null ? null : workspace.getEmbeddingModel();
        String chatModel = workspace == null ? null : workspace.getChatModel();

        logger.info("Computing embedding for query");
        List<Double> queryEmbedding = googleGenerativeAiService.embedText(query, embeddingModel);
        if (queryEmbedding != null) {
            logger.debug("Query embedding dimension: {}", queryEmbedding.size());
        }

        logger.info("Finding nearest chunks using Supabase vector search");
        int configuredTopK = topK > 0 ? topK : workspace == null || workspace.getTopK() <= 0 ? 4 : workspace.getTopK();
        int boundedTopK = Math.max(1, Math.min(configuredTopK, 10));
        List<DocumentChunk> nearest = vectorStoreService.searchNearest(queryEmbedding, boundedTopK, workspaceId, documentIds);
        if (nearest.isEmpty()) {
            logger.warn("No matching chunks found in Supabase");
            return new QueryResponse("No documents have been uploaded yet.", List.of());
        }

        logger.info("Selected {} nearest chunks", nearest.size());

        List<String> context = nearest.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        logger.info("Generating answer based on {} context excerpts", context.size());
        String answer = googleGenerativeAiService.createAnswer(query, context, chatModel);
        
        List<SourceCitation> sources = new ArrayList<>();
        for (int i = 0; i < nearest.size(); i++) {
            sources.add(toCitation(nearest.get(i), i));
        }
        
        logger.info("Answer generated with {} source document(s)", sources.size());
        return new QueryResponse(answer, sources);
    }

    public void deleteDocument(String documentId) {
        vectorStoreService.deleteDocument(documentId);
    }

    private SourceCitation toCitation(DocumentChunk chunk, int chunkIndex) {
        Double similarity = chunk.getDistance() == null ? null : Math.max(0, 1 - chunk.getDistance());
        return new SourceCitation(
                chunk.getId(),
                chunk.getDocumentId(),
                chunk.getFileName(),
                chunkIndex,
                similarity == null ? null : Math.round(similarity * 100.0) / 100.0,
                trimSnippet(chunk.getContent())
        );
    }

    private String trimSnippet(String content) {
        if (content == null || content.length() <= 500) {
            return content;
        }
        return content.substring(0, 497).trim() + "...";
    }

    private String sha256Hex(byte[] payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 hashing is not available.", ex);
        }
    }

    private String documentIdForHash(String workspaceId, String fileHash) {
        String key = (workspaceId == null ? "" : workspaceId) + "|" + fileHash;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String normalizeWorkspaceId(String workspaceId) {
        return workspaceId == null || workspaceId.isBlank() ? null : workspaceId;
    }

    private String normalizeFileName(String fileName) {
        return fileName == null || fileName.isBlank() ? "uploaded-file" : fileName;
    }
}
