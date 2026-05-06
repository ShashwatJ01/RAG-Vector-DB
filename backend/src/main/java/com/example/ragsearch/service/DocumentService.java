package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.QueryResponse;
import com.example.ragsearch.model.UploadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final Map<String, DocumentMetadata> documents = new ConcurrentHashMap<>();
    private final List<DocumentChunk> chunks = new CopyOnWriteArrayList<>();
    private final GoogleGenerativeAiService googleGenerativeAiService;

    public DocumentService(GoogleGenerativeAiService googleGenerativeAiService) {
        this.googleGenerativeAiService = googleGenerativeAiService;
        logger.info("DocumentService initialized");
    }

    public List<DocumentMetadata> listDocuments() {
        logger.debug("Listing {} documents", documents.size());
        return new ArrayList<>(documents.values());
    }

    public UploadResponse ingestFile(MultipartFile file) throws IOException {
        logger.info("Starting file ingestion: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        
        long startTime = System.currentTimeMillis();
        
        logger.info("Step 1: Extracting text from file");
        String text = extractText(file);
        logger.info("Step 1 completed: Extracted {} characters", text.length());
        if (text.isBlank()) {
            throw new IllegalArgumentException("No readable text was found in the uploaded file.");
        }
        
        logger.info("Step 2: Generating document ID and metadata");
        String documentId = UUID.randomUUID().toString();
        DocumentMetadata metadata = new DocumentMetadata(documentId, file.getOriginalFilename(), text.length());
        documents.put(documentId, metadata);
        logger.info("Step 2 completed: Document {} stored with ID: {}", file.getOriginalFilename(), documentId);
        
        logger.info("Step 3: Splitting text into chunks");
        List<String> chunkTexts = splitToChunks(text, 900, 200);
        logger.info("Step 3 completed: Document split into {} chunks", chunkTexts.size());
        
        logger.info("Step 4: Computing embeddings for {} chunks", chunkTexts.size());
        long embeddingStartTime = System.currentTimeMillis();
        List<List<Double>> embeddings = googleGenerativeAiService.embedTexts(chunkTexts);
        long embeddingEndTime = System.currentTimeMillis();
        logger.info("Step 4 completed: Embeddings computed in {} ms", (embeddingEndTime - embeddingStartTime));
        
        logger.info("Step 5: Storing chunks with embeddings");
        for (int i = 0; i < chunkTexts.size(); i++) {
            String chunkId = UUID.randomUUID().toString();
            DocumentChunk chunk = new DocumentChunk(chunkId, documentId, chunkTexts.get(i), embeddings.get(i));
            chunks.add(chunk);
            if (embeddings.get(i) != null) {
                logger.debug("Stored chunk [{}] with embedding dimension: {}", i + 1, embeddings.get(i).size());
            }
        }
        logger.info("Step 5 completed: All {} chunks stored", chunkTexts.size());
        
        long endTime = System.currentTimeMillis();
        logger.info("Ingestion complete in {} ms. Total chunks in system: {}", (endTime - startTime), chunks.size());
        return new UploadResponse(documentId, file.getOriginalFilename(), file.getSize());
    }

    public QueryResponse answerQuery(String query) {
        logger.info("Processing query: '{}'", query);
        
        if (chunks.isEmpty()) {
            logger.warn("Query received but no documents uploaded yet");
            return new QueryResponse("No documents have been uploaded yet.", List.of());
        }

        logger.debug("Total chunks available: {}", chunks.size());
        
        logger.info("Computing embedding for query");
        List<Double> queryEmbedding = googleGenerativeAiService.embedText(query);
        if (queryEmbedding != null) {
            logger.debug("Query embedding dimension: {}", queryEmbedding.size());
        }
        
        logger.info("Finding nearest chunks using cosine similarity");
        List<DocumentChunk> nearest = chunks.stream()
                .map(chunk -> {
                    double similarity = cosineSimilarity(queryEmbedding, chunk.getEmbedding());
                    logger.debug("Chunk similarity score: {}", similarity);
                    return Map.entry(chunk, similarity);
                })
                .sorted(Comparator.comparingDouble(Map.Entry<DocumentChunk, Double>::getValue).reversed())
                .limit(4)
                .peek(entry -> logger.debug("Selected chunk with similarity: {}", entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info("Selected {} nearest chunks", nearest.size());

        List<String> context = nearest.stream()
                .map(DocumentChunk::getContent)
                .collect(Collectors.toList());

        logger.info("Generating answer based on {} context excerpts", context.size());
        String answer = googleGenerativeAiService.createAnswer(query, context);
        
        List<String> sources = nearest.stream()
                .map(DocumentChunk::getDocumentId)
                .distinct()
                .collect(Collectors.toList());
        
        logger.info("Answer generated with {} source document(s)", sources.size());
        return new QueryResponse(answer, sources);
    }

    private String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".pdf")) {
            logger.info("Processing PDF file: {}", filename);
            try (PDDocument document = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                logger.info("Extracted {} characters from PDF", text.length());
                return text;
            }
        } else {
            logger.info("Processing text file: {}", filename);
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            logger.info("Read {} characters from text file", text.length());
            return text;
        }
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
            start = end - overlap;
            if (start < 0) {
                start = 0;
            }
        }
        return chunks;
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size()) {
            return -1.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double va = a.get(i);
            double vb = b.get(i);
            dot += va * vb;
            normA += va * va;
            normB += vb * vb;
        }
        if (normA == 0 || normB == 0) {
            return -1.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
