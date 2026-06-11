package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.DocumentStatus;
import com.example.ragsearch.model.DocumentStatusResponse;
import com.example.ragsearch.model.IngestionJob;
import com.example.ragsearch.model.QueryResponse;
import com.example.ragsearch.model.RerankComparison;
import com.example.ragsearch.model.SearchMode;
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
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private static final int MAX_FINAL_TOP_K = 10;
    private static final int DEFAULT_RERANK_TOP_N = 20;
    private static final int MAX_RERANK_TOP_N = 50;
    private static final Set<String> KEYWORD_STOP_WORDS = Set.of(
            "about", "answer", "could", "data", "did", "document", "documents", "does", "file", "find",
            "for", "from", "give", "had", "has", "have", "how", "many", "much", "pertaining", "please",
            "show", "tell", "that", "the", "this", "uploaded", "was", "were", "what", "when", "where",
            "which", "who", "why", "with", "would", "are", "can", "entail", "entails", "is", "me"
    );
    private final GoogleGenerativeAiService googleGenerativeAiService;
    private final VectorStoreService vectorStoreService;
    private final RerankingService rerankingService;
    private final WorkspaceService workspaceService;
    private final IngestionJobRepository ingestionJobRepository;
    private final int maxRetries;

    public DocumentService(GoogleGenerativeAiService googleGenerativeAiService,
                           VectorStoreService vectorStoreService,
                           RerankingService rerankingService,
                           WorkspaceService workspaceService,
                           IngestionJobRepository ingestionJobRepository,
                           @Value("${ingestion.worker.max-retries:3}") int maxRetries) {
        this.googleGenerativeAiService = googleGenerativeAiService;
        this.vectorStoreService = vectorStoreService;
        this.rerankingService = rerankingService;
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
        return answerQuery(query, workspaceId, documentIds, topK, SearchMode.SEMANTIC, null, null);
    }

    public QueryResponse answerQuery(String query,
                                     String workspaceId,
                                     List<String> documentIds,
                                     int topK,
                                     SearchMode searchMode,
                                     Double semanticWeight,
                                     Double keywordWeight) {
        return answerQuery(query, workspaceId, documentIds, topK, searchMode, semanticWeight, keywordWeight, null, true, false);
    }

    public QueryResponse answerQuery(String query,
                                     String workspaceId,
                                     List<String> documentIds,
                                     int topK,
                                     SearchMode searchMode,
                                     Double semanticWeight,
                                     Double keywordWeight,
                                     Integer topN,
                                     Boolean rerank,
                                     Boolean compareReranking) {
        logger.info("Processing query: '{}'", query);
        Workspace workspace = workspaceService.getWorkspace(workspaceId);
        String embeddingModel = workspace == null ? null : workspace.getEmbeddingModel();
        String chatModel = workspace == null ? null : workspace.getChatModel();

        int configuredTopK = topK > 0 ? topK : workspace == null || workspace.getTopK() <= 0 ? 4 : workspace.getTopK();
        int boundedTopK = Math.max(1, Math.min(configuredTopK, MAX_FINAL_TOP_K));
        boolean shouldRerank = rerank == null || rerank;
        boolean shouldCompareReranking = shouldRerank && Boolean.TRUE.equals(compareReranking);
        int retrievalTopN = resolveRetrievalTopN(topN, boundedTopK, shouldRerank);
        SearchMode resolvedSearchMode = searchMode == null ? SearchMode.SEMANTIC : searchMode;
        logger.info("Retrieving {} candidate chunk(s), then selecting final top {}", retrievalTopN, boundedTopK);
        List<DocumentChunk> candidates = searchChunks(
                query,
                embeddingModel,
                retrievalTopN,
                workspaceId,
                documentIds,
                resolvedSearchMode,
                resolveWeight(semanticWeight),
                resolveWeight(keywordWeight)
        );
        if (candidates.isEmpty()) {
            logger.warn("No matching chunks found in Supabase");
            return new QueryResponse("No documents have been uploaded yet.", List.of());
        }

        rerankingService.applyOriginalRanks(candidates);
        List<DocumentChunk> baselineChunks = shouldCompareReranking || !shouldRerank
                ? rerankingService.selectOriginalTopK(candidates, boundedTopK)
                : List.of();
        List<SourceCitation> baselineSources = shouldCompareReranking
                ? toCitations(baselineChunks)
                : List.of();

        List<DocumentChunk> finalChunks = shouldRerank
                ? rerankingService.rerank(query, candidates, boundedTopK)
                : baselineChunks;

        logger.info("Selected {} final chunk(s) from {} candidate(s), rerank={}",
                finalChunks.size(), candidates.size(), shouldRerank);
        for (int i = 0; i < finalChunks.size(); i++) {
            DocumentChunk chunk = finalChunks.get(i);
            logger.debug("Selected chunk {} documentId={} originalRank={} rerankScore={} score={} preview={}",
                    i + 1,
                    chunk.getDocumentId(),
                    chunk.getOriginalRank(),
                    chunk.getRerankScore(),
                    chunk.getDistance() == null ? null : Math.round(Math.max(0, 1 - chunk.getDistance()) * 100.0) / 100.0,
                    trimForLog(chunk.getContent())
            );
        }

        List<String> context = finalChunks.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        logger.info("Generating answer based on {} context excerpts", context.size());
        String answer = googleGenerativeAiService.createAnswer(query, context, chatModel);

        List<SourceCitation> sources = toCitations(finalChunks);
        RerankComparison comparison = null;
        if (shouldCompareReranking) {
            logger.info("Generating baseline answer without reranking for comparison");
            List<String> baselineContext = baselineChunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.toList());
            String baselineAnswer = googleGenerativeAiService.createAnswer(query, baselineContext, chatModel);
            comparison = new RerankComparison(baselineAnswer, baselineSources, countSourceOverlap(sources, baselineSources));
        }

        logger.info("Answer generated with {} source document(s)", sources.size());
        return new QueryResponse(answer, sources, "Grounded", shouldRerank, retrievalTopN, boundedTopK, comparison);
    }

    private List<DocumentChunk> searchChunks(String query,
                                             String embeddingModel,
                                             int limit,
                                             String workspaceId,
                                             List<String> documentIds,
                                             SearchMode searchMode,
                                             double semanticWeight,
                                             double keywordWeight) {
        logger.info("Finding chunks using {} search", searchMode.name().toLowerCase());
        String keywordQuery = toKeywordSearchText(query);
        if (!keywordQuery.equals(query)) {
            logger.debug("Keyword search text normalized from '{}' to '{}'", query, keywordQuery);
        }

        if (searchMode == SearchMode.KEYWORD) {
            return vectorStoreService.searchKeyword(keywordQuery, limit, workspaceId, documentIds);
        }

        logger.info("Computing embedding for query");
        List<Double> queryEmbedding = googleGenerativeAiService.embedText(query, embeddingModel);
        if (queryEmbedding != null) {
            logger.debug("Query embedding dimension: {}", queryEmbedding.size());
        }

        if (searchMode == SearchMode.HYBRID) {
            return vectorStoreService.searchHybrid(keywordQuery, queryEmbedding, limit, workspaceId, documentIds, semanticWeight, keywordWeight);
        }

        return vectorStoreService.searchNearest(queryEmbedding, limit, workspaceId, documentIds);
    }

    public void deleteDocument(String documentId) {
        vectorStoreService.deleteDocument(documentId);
    }

    private List<SourceCitation> toCitations(List<DocumentChunk> chunks) {
        List<SourceCitation> sources = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            sources.add(toCitation(chunks.get(i), i));
        }
        return sources;
    }

    private SourceCitation toCitation(DocumentChunk chunk, int chunkIndex) {
        Double similarity = chunk.getDistance() == null ? null : Math.max(0, 1 - chunk.getDistance());
        return new SourceCitation(
                chunk.getId(),
                chunk.getDocumentId(),
                chunk.getFileName(),
                chunkIndex,
                similarity == null ? null : Math.round(similarity * 100.0) / 100.0,
                chunk.getOriginalRank(),
                chunk.getRerankScore(),
                chunk.getFinalRank(),
                trimSnippet(chunk.getContent())
        );
    }

    private int countSourceOverlap(List<SourceCitation> rerankedSources, List<SourceCitation> baselineSources) {
        Set<String> baselineIds = baselineSources.stream()
                .map(SourceCitation::getId)
                .collect(Collectors.toSet());
        return (int) rerankedSources.stream()
                .map(SourceCitation::getId)
                .filter(baselineIds::contains)
                .count();
    }

    private int resolveRetrievalTopN(Integer requestedTopN, int finalTopK, boolean shouldRerank) {
        if (!shouldRerank) {
            return finalTopK;
        }

        int requestedOrDefault = requestedTopN == null || requestedTopN <= 0
                ? Math.max(DEFAULT_RERANK_TOP_N, finalTopK * 5)
                : requestedTopN;
        int bounded = Math.max(finalTopK, requestedOrDefault);
        return Math.min(Math.max(DEFAULT_RERANK_TOP_N, bounded), MAX_RERANK_TOP_N);
    }

    private String trimSnippet(String content) {
        if (content == null || content.length() <= 500) {
            return content;
        }
        return content.substring(0, 497).trim() + "...";
    }

    private String trimForLog(String content) {
        if (content == null) {
            return "";
        }
        String compact = content.replaceAll("\\s+", " ").trim();
        return compact.length() <= 300 ? compact : compact.substring(0, 297) + "...";
    }

    private double resolveWeight(Double weight) {
        if (weight == null || weight <= 0) {
            return 1.0;
        }
        return Math.min(weight, 5.0);
    }

    private String toKeywordSearchText(String query) {
        if (query == null || query.isBlank()) {
            return query;
        }

        List<String> terms = Arrays.stream(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .map(String::trim)
                .filter(term -> term.length() > 2)
                .filter(term -> !KEYWORD_STOP_WORDS.contains(term))
                .distinct()
                .collect(Collectors.toList());

        if (terms.isEmpty()) {
            return query;
        }

        return String.join(" OR ", terms);
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
