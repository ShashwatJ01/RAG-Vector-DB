package com.example.ragsearch.controller;

import com.example.ragsearch.model.QueryRequest;
import com.example.ragsearch.model.QueryResponse;
import com.example.ragsearch.model.SearchMode;
import com.example.ragsearch.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/query")
public class QueryController {
    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private final DocumentService documentService;

    public QueryController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<QueryResponse> ask(@RequestBody QueryRequest request) {
        logger.info("Received query request");
        try {
            if (request.getQuery() == null || request.getQuery().isBlank()) {
                logger.warn("Query is null or blank");
                return ResponseEntity.badRequest().build();
            }
            logger.info("Processing query: '{}'", request.getQuery());
            int topK = request.getTopK() == null ? 0 : request.getTopK();
            SearchMode searchMode = SearchMode.from(request.getSearchMode());
            QueryResponse response = documentService.answerQuery(
                    request.getQuery(),
                    request.getWorkspaceId(),
                    request.getDocumentIds(),
                    topK,
                    searchMode,
                    request.getSemanticWeight(),
                    request.getKeywordWeight(),
                    request.getTopN(),
                    request.getRerank(),
                    request.getCompareReranking()
            );
            logger.info("Query processed successfully. Answer length: {} chars, sources: {}", 
                    response.getAnswer().length(), response.getSources().size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            logger.error("Invalid argument in query: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException ex) {
            logger.error("Runtime error processing query: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            logger.error("Unexpected error processing query: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
