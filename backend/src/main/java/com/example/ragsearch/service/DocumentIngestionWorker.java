package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentStatus;
import com.example.ragsearch.model.IngestionJob;
import com.example.ragsearch.model.Workspace;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DocumentIngestionWorker {
    private static final Logger logger = LoggerFactory.getLogger(DocumentIngestionWorker.class);
    private static final int MAX_CHUNK_SIZE = 900;
    private static final int CHUNK_OVERLAP = 200;

    private final IngestionJobRepository ingestionJobRepository;
    private final VectorStoreService vectorStoreService;
    private final GoogleGenerativeAiService googleGenerativeAiService;
    private final WorkspaceService workspaceService;
    private final boolean enabled;
    private final int maxJobsPerPoll;
    private final long retryBackoffSeconds;
    private final int defaultEmbeddingDimension;

    public DocumentIngestionWorker(IngestionJobRepository ingestionJobRepository,
                                   VectorStoreService vectorStoreService,
                                   GoogleGenerativeAiService googleGenerativeAiService,
                                   WorkspaceService workspaceService,
                                   @Value("${ingestion.worker.enabled:true}") boolean enabled,
                                   @Value("${ingestion.worker.max-jobs-per-poll:1}") int maxJobsPerPoll,
                                   @Value("${ingestion.worker.retry-backoff-seconds:15}") long retryBackoffSeconds,
                                   @Value("${google.embedding.output-dimensionality}") int defaultEmbeddingDimension) {
        this.ingestionJobRepository = ingestionJobRepository;
        this.vectorStoreService = vectorStoreService;
        this.googleGenerativeAiService = googleGenerativeAiService;
        this.workspaceService = workspaceService;
        this.enabled = enabled;
        this.maxJobsPerPoll = maxJobsPerPoll;
        this.retryBackoffSeconds = retryBackoffSeconds;
        this.defaultEmbeddingDimension = defaultEmbeddingDimension;
    }

    @Scheduled(fixedDelayString = "${ingestion.worker.poll-ms:2000}")
    public void poll() {
        if (!enabled) {
            return;
        }

        for (int i = 0; i < maxJobsPerPoll; i++) {
            Optional<IngestionJob> job = ingestionJobRepository.claimNextJob();
            if (job.isEmpty()) {
                return;
            }
            process(job.get());
        }
    }

    private void process(IngestionJob job) {
        logger.info("Processing ingestion job={} documentId={} retry={}/{}", job.getId(), job.getDocumentId(),
                job.getRetryCount(), job.getMaxRetries());
        vectorStoreService.updateDocumentStatus(job.getDocumentId(), DocumentStatus.PROCESSING, null);

        try {
            Workspace workspace = workspaceService.getWorkspace(job.getWorkspaceId());
            String embeddingModel = workspace == null ? null : workspace.getEmbeddingModel();
            int embeddingDimension = workspace == null ? defaultEmbeddingDimension : workspace.getEmbeddingDimension();

            String text = extractText(job.getPayload(), job.getFileName());
            if (text.isBlank()) {
                throw new IllegalArgumentException("No readable text was found in the uploaded file.");
            }

            List<String> chunkTexts = splitToChunks(text, MAX_CHUNK_SIZE, CHUNK_OVERLAP);
            if (chunkTexts.isEmpty()) {
                throw new IllegalArgumentException("No indexable chunks were produced from the uploaded file.");
            }

            logger.info("Embedding {} chunk(s) for documentId={}", chunkTexts.size(), job.getDocumentId());
            List<List<Double>> embeddings = googleGenerativeAiService.embedTexts(chunkTexts, embeddingModel);
            if (embeddings.size() != chunkTexts.size()) {
                throw new IllegalStateException("Embedding count did not match chunk count.");
            }

            vectorStoreService.deleteDocumentChunks(job.getDocumentId());
            for (int i = 0; i < chunkTexts.size(); i++) {
                DocumentChunk chunk = new DocumentChunk(UUID.randomUUID().toString(), job.getDocumentId(), chunkTexts.get(i), embeddings.get(i));
                vectorStoreService.saveChunk(chunk);
            }

            vectorStoreService.markDocumentIndexed(job.getDocumentId(), text.length(), embeddingModel, embeddingDimension);
            ingestionJobRepository.markIndexed(job.getId());
            logger.info("Indexed documentId={} from ingestion job={}", job.getDocumentId(), job.getId());
        } catch (Exception ex) {
            handleFailure(job, ex);
        }
    }

    private void handleFailure(IngestionJob job, Exception ex) {
        int nextRetryCount = job.getRetryCount() + 1;
        String errorMessage = truncateError(ex);
        logger.warn("Ingestion job={} failed on attempt {} of {}: {}", job.getId(), nextRetryCount,
                job.getMaxRetries(), errorMessage, ex);

        if (nextRetryCount < job.getMaxRetries()) {
            OffsetDateTime availableAt = OffsetDateTime.now(ZoneOffset.UTC)
                    .plusSeconds(Math.max(1, retryBackoffSeconds) * nextRetryCount);
            ingestionJobRepository.markQueuedForRetry(job.getId(), nextRetryCount, errorMessage, availableAt);
            vectorStoreService.updateDocumentStatus(job.getDocumentId(), DocumentStatus.QUEUED, errorMessage);
            return;
        }

        ingestionJobRepository.markFailed(job.getId(), nextRetryCount, errorMessage);
        vectorStoreService.updateDocumentStatus(job.getDocumentId(), DocumentStatus.FAILED, errorMessage);
    }

    private String extractText(byte[] payload, String fileName) throws IOException {
        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(payload)) {
                return new PDFTextStripper().getText(document);
            }
        }
        return new String(payload, StandardCharsets.UTF_8);
    }

    private List<String> splitToChunks(String text, int maxChunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            if (end == text.length()) {
                break;
            }
            start = Math.max(0, end - overlap);
        }
        return chunks;
    }

    private String truncateError(Exception ex) {
        String message = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
        return message.length() <= 1000 ? message : message.substring(0, 997) + "...";
    }
}
