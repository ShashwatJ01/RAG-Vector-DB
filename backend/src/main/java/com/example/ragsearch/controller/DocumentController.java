package com.example.ragsearch.controller;

import com.example.ragsearch.model.DocumentMetadata;
import com.example.ragsearch.model.DocumentStatusResponse;
import com.example.ragsearch.model.UploadResponse;
import com.example.ragsearch.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/documents")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                         @RequestParam(value = "workspaceId", required = false) String workspaceId) {
        logger.info("Received upload request with {} file(s)", files.length);
        for (int i = 0; i < files.length; i++) {
            logger.info("File [{}]: name='{}', size={} bytes", i + 1, files[i].getOriginalFilename(), files[i].getSize());
        }
        
        List<UploadResponse> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                UploadResponse response = documentService.ingestFile(file, workspaceId);
                uploaded.add(response);
                logger.info("Successfully uploaded file: {}", file.getOriginalFilename());
            }
            logger.info("Upload complete. {} file(s) processed", uploaded.size());
            return ResponseEntity.ok(uploaded);
        } catch (IOException ex) {
            logger.error("IO error during file upload: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not read uploaded file: " + ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid argument during file upload: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (RuntimeException ex) {
            logger.error("Runtime error during file upload: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error during file upload: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected upload error: " + ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentMetadata>> listDocuments(@RequestParam(value = "workspaceId", required = false) String workspaceId) {
        logger.info("Fetching document list");
        List<DocumentMetadata> docs = documentService.listDocuments(workspaceId);
        logger.info("Returning {} documents", docs.size());
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<?> getDocumentStatus(@PathVariable String documentId) {
        try {
            DocumentStatusResponse status = documentService.getDocumentStatus(documentId);
            return ResponseEntity.ok(status);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        logger.info("Deleting document {}", documentId);
        documentService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
