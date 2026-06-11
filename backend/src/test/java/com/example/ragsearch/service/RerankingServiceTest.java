package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RerankingServiceTest {
    private final RerankingService rerankingService = new RerankingService();

    @Test
    void rerankPromotesAnswerBearingChunkFromBroadCandidates() {
        List<DocumentChunk> candidates = List.of(
                chunk("c1", "This section explains semantic embeddings and vector indexes.", 0.05),
                chunk("c2", "Invoices and receipts must be retained for seven years after the fiscal year closes.", 0.24),
                chunk("c3", "The support policy covers account access and password resets.", 0.18)
        );

        List<DocumentChunk> reranked = rerankingService.rerank(
                "What is the retention period for invoices?",
                candidates,
                1
        );

        DocumentChunk topChunk = reranked.get(0);
        assertEquals("c2", topChunk.getId());
        assertEquals(2, topChunk.getOriginalRank());
        assertEquals(1, topChunk.getFinalRank());
        assertNotNull(topChunk.getRerankScore());
    }

    @Test
    void selectOriginalTopKStoresOriginalAndFinalRanksWithoutRerankScore() {
        List<DocumentChunk> candidates = List.of(
                chunk("c1", "First result", 0.05),
                chunk("c2", "Second result", 0.10)
        );

        rerankingService.applyOriginalRanks(candidates);
        List<DocumentChunk> selected = rerankingService.selectOriginalTopK(candidates, 1);

        assertEquals("c1", selected.get(0).getId());
        assertEquals(1, selected.get(0).getOriginalRank());
        assertEquals(1, selected.get(0).getFinalRank());
        assertNull(selected.get(0).getRerankScore());
    }

    private DocumentChunk chunk(String id, String content, double distance) {
        return new DocumentChunk(id, "doc-1", "policy.txt", content, null, distance);
    }
}
