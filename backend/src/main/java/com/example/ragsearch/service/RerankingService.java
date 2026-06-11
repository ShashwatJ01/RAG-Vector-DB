package com.example.ragsearch.service;

import com.example.ragsearch.model.DocumentChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RerankingService {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[a-z0-9]+");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?");
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "answer", "before", "could", "document", "documents", "does", "file",
            "find", "from", "give", "have", "into", "please", "show", "tell", "that", "the",
            "this", "uploaded", "what", "when", "where", "which", "while", "with", "would",
            "your", "are", "can", "did", "for", "has", "how", "is", "me", "of", "to", "was"
    );

    public void applyOriginalRanks(List<DocumentChunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            chunk.setOriginalRank(i + 1);
            chunk.setFinalRank(null);
            chunk.setRerankScore(null);
        }
    }

    public List<DocumentChunk> selectOriginalTopK(List<DocumentChunk> chunks, int topK) {
        int limit = Math.min(Math.max(topK, 0), chunks.size());
        List<DocumentChunk> selected = new ArrayList<>(chunks.subList(0, limit));
        applyFinalRanks(selected);
        return selected;
    }

    public List<DocumentChunk> rerank(String query, List<DocumentChunk> candidates, int topK) {
        applyOriginalRanks(candidates);
        QueryFeatures queryFeatures = QueryFeatures.from(query);

        List<DocumentChunk> ranked = new ArrayList<>(candidates);
        for (DocumentChunk chunk : ranked) {
            chunk.setRerankScore(score(queryFeatures, chunk));
        }

        ranked.sort(Comparator
                .comparing(DocumentChunk::getRerankScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(DocumentChunk::getOriginalRank, Comparator.nullsLast(Comparator.naturalOrder())));

        int limit = Math.min(Math.max(topK, 0), ranked.size());
        List<DocumentChunk> selected = new ArrayList<>(ranked.subList(0, limit));
        applyFinalRanks(selected);
        return selected;
    }

    private void applyFinalRanks(List<DocumentChunk> chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setFinalRank(i + 1);
        }
    }

    private double score(QueryFeatures queryFeatures, DocumentChunk chunk) {
        double retrievalScore = retrievalScore(chunk);
        if (queryFeatures.terms().isEmpty()) {
            return roundScore(retrievalScore);
        }

        String chunkText = (chunk.getContent() == null ? "" : chunk.getContent())
                + " "
                + (chunk.getFileName() == null ? "" : chunk.getFileName());
        List<String> chunkTerms = tokenize(chunkText);
        Set<String> chunkTermSet = new HashSet<>(chunkTerms);
        long matchedTerms = queryFeatures.terms().stream()
                .filter(chunkTermSet::contains)
                .count();

        double termRecall = matchedTerms / (double) queryFeatures.terms().size();
        double termDensity = termDensity(queryFeatures.terms(), chunkTerms);
        double phraseCoverage = phraseCoverage(queryFeatures.phrases(), chunk.getContent());
        double numberCoverage = numberCoverage(queryFeatures.numbers(), chunk.getContent());
        double lexicalScore = (0.68 * termRecall) + (0.22 * phraseCoverage) + (0.10 * numberCoverage);

        return roundScore((0.58 * lexicalScore) + (0.32 * retrievalScore) + (0.10 * termDensity));
    }

    private double retrievalScore(DocumentChunk chunk) {
        if (chunk.getDistance() == null) {
            return 0.0;
        }
        return clamp(1.0 - chunk.getDistance());
    }

    private double termDensity(Set<String> queryTerms, List<String> chunkTerms) {
        if (queryTerms.isEmpty() || chunkTerms.isEmpty()) {
            return 0.0;
        }

        long matches = chunkTerms.stream()
                .filter(queryTerms::contains)
                .count();
        double expectedWindow = Math.min(chunkTerms.size(), queryTerms.size() * 8.0);
        return clamp(matches / Math.max(1.0, expectedWindow));
    }

    private double phraseCoverage(List<String> queryPhrases, String content) {
        if (queryPhrases.isEmpty() || content == null || content.isBlank()) {
            return 0.0;
        }

        String normalizedContent = content.toLowerCase(Locale.ROOT);
        long matchedPhrases = queryPhrases.stream()
                .filter(normalizedContent::contains)
                .count();
        return matchedPhrases / (double) queryPhrases.size();
    }

    private double numberCoverage(Set<String> queryNumbers, String content) {
        if (queryNumbers.isEmpty() || content == null || content.isBlank()) {
            return 0.0;
        }

        Set<String> contentNumbers = numbers(content);
        long matchedNumbers = queryNumbers.stream()
                .filter(contentNumbers::contains)
                .count();
        return matchedNumbers / (double) queryNumbers.size();
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        Matcher matcher = TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT));
        List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (token.length() > 2 && !STOP_WORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static Set<String> numbers(String text) {
        Matcher matcher = NUMBER_PATTERN.matcher(text == null ? "" : text);
        Set<String> values = new LinkedHashSet<>();
        while (matcher.find()) {
            values.add(matcher.group());
        }
        return values;
    }

    private static List<String> phrases(List<String> terms) {
        if (terms.size() < 2) {
            return List.of();
        }

        List<String> phrases = new ArrayList<>();
        for (int i = 0; i < terms.size() - 1; i++) {
            phrases.add(terms.get(i) + " " + terms.get(i + 1));
        }
        return phrases;
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double roundScore(double score) {
        return Math.round(clamp(score) * 10_000.0) / 10_000.0;
    }

    private record QueryFeatures(Set<String> terms, List<String> phrases, Set<String> numbers) {
        private static QueryFeatures from(String query) {
            List<String> queryTokens = tokenize(query);
            List<String> uniqueTerms = queryTokens.stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toCollection(LinkedHashSet::new),
                            ArrayList::new
                    ));

            return new QueryFeatures(
                    new LinkedHashSet<>(uniqueTerms),
                    RerankingService.phrases(uniqueTerms),
                    RerankingService.numbers(query)
            );
        }
    }
}
