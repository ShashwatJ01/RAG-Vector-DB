package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.QueryResponse;
import com.example.ragsearch.model.UploadResponse;
import com.example.ragsearch.service.VectorStoreService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private final GoogleGenerativeAiService googleGenerativeAiService;
    private final VectorStoreService vectorStoreService;

    public DocumentService(GoogleGenerativeAiService googleGenerativeAiService,
                           VectorStoreService vectorStoreService) {
        this.googleGenerativeAiService = googleGenerativeAiService;
        this.vectorStoreService = vectorStoreService;
        logger.info("DocumentService initialized");
    }

    public List<DocumentMetadata> listDocuments() {
        logger.debug("Listing documents from vector store");
        return vectorStoreService.listDocuments();
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
        logger.info("Step 2 completed: Document {} created with ID: {}", file.getOriginalFilename(), documentId);
        
        logger.info("Step 3: Splitting text into chunks");
        List<String> chunkTexts = splitToChunks(text, 900, 200);
        logger.info("Step 3 completed: Document split into {} chunks", chunkTexts.size());
        
        logger.info("Step 4: Computing embeddings for {} chunks", chunkTexts.size());
        long embeddingStartTime = System.currentTimeMillis();
        List<List<Double>> embeddings = googleGenerativeAiService.embedTexts(chunkTexts);
        long embeddingEndTime = System.currentTimeMillis();
        logger.info("Step 4 completed: Embeddings computed in {} ms", (embeddingEndTime - embeddingStartTime));
        
        logger.info("Step 5: Storing document metadata and chunk embeddings to Supabase");
        vectorStoreService.saveDocumentMetadata(metadata);
        for (int i = 0; i < chunkTexts.size(); i++) {
            String chunkId = UUID.randomUUID().toString();
            DocumentChunk chunk = new DocumentChunk(chunkId, documentId, chunkTexts.get(i), embeddings.get(i));
            vectorStoreService.saveChunk(chunk);
            if (embeddings.get(i) != null) {
                logger.debug("Stored chunk [{}] with embedding dimension: {}", i + 1, embeddings.get(i).size());
            }
        }
        logger.info("Step 5 completed: All {} chunks stored in Supabase", chunkTexts.size());
        
        long endTime = System.currentTimeMillis();
        logger.info("Ingestion complete in {} ms. Document and embeddings stored in Supabase", (endTime - startTime));
        return new UploadResponse(documentId, file.getOriginalFilename(), file.getSize());
    }

    public QueryResponse answerQuery(String query) {
        logger.info("Processing query: '{}'", query);

        logger.info("Computing embedding for query");
        List<Double> queryEmbedding = googleGenerativeAiService.embedText(query);
        if (queryEmbedding != null) {
            logger.debug("Query embedding dimension: {}", queryEmbedding.size());
        }

        logger.info("Finding nearest chunks using Supabase vector search");
        List<DocumentChunk> nearest = vectorStoreService.searchNearest(queryEmbedding, 4);
        if (nearest.isEmpty()) {
            logger.warn("No matching chunks found in Supabase");
            return new QueryResponse("No documents have been uploaded yet.", List.of());
        }

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

}
