package com.example.ragsearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleGenerativeAiService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleGenerativeAiService.class);
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;
    private final String chatModel;
    private final String embeddingModel;
    private final int embeddingOutputDimensionality;

    public GoogleGenerativeAiService(RestTemplate restTemplate,
                                      @Value("${google.api.key}") String apiKey,
                                      @Value("${google.api.url}") String apiUrl,
                                      @Value("${google.chat.model}") String chatModel,
                                      @Value("${google.embedding.model}") String embeddingModel,
                                      @Value("${google.embedding.output-dimensionality}") int embeddingOutputDimensionality) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.embeddingOutputDimensionality = embeddingOutputDimensionality;
        logger.info("GoogleGenerativeAiService initialized with chat model: {}, embedding model: {}, embedding dimensions: {}",
                chatModel, embeddingModel, embeddingOutputDimensionality);
    }

    public List<List<Double>> embedTexts(List<String> texts) {
        return embedTexts(texts, embeddingModel);
    }

    public List<List<Double>> embedTexts(List<String> texts, String requestedEmbeddingModel) {
        long startedAt = System.currentTimeMillis();
        logger.info("embedding batch started textCount={} requestedModel={}", texts.size(), requestedEmbeddingModel);
        List<List<Double>> embeddings = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            logger.debug("embedding batch item started index={} total={} textLength={}", i + 1, texts.size(), lengthOf(text));
            List<Double> embedding = embedText(text, requestedEmbeddingModel);
            embeddings.add(embedding);
            if (embedding != null) {
                logger.debug("embedding batch item completed index={} dimension={}", i + 1, embedding.size());
            }
        }
        logger.info("embedding batch completed textCount={} durationMs={}",
                embeddings.size(),
                System.currentTimeMillis() - startedAt);
        return embeddings;
    }

    public List<Double> embedText(String text) {
        return embedText(text, embeddingModel);
    }

    public List<Double> embedText(String text, String requestedEmbeddingModel) {
        String model = requestedEmbeddingModel == null || requestedEmbeddingModel.isBlank()
                ? embeddingModel
                : requestedEmbeddingModel;
        long startedAt = System.currentTimeMillis();
        logger.info("embedding request started model={} textLength={} outputDimensionality={}",
                model,
                text == null ? 0 : text.length(),
                embeddingOutputDimensionality);
        Map<String, Object> request = Map.of(
                "model", modelResourceName(model),
                "content", Map.of("parts", List.of(Map.of("text", text))),
                "output_dimensionality", embeddingOutputDimensionality
        );
        Map<String, Object> response = post(apiUrl + "/v1beta/models/" + modelPathName(model) + ":embedContent", request);
        List<Double> embedding = parseEmbedding(response);
        if (embedding == null || embedding.isEmpty()) {
            throw new RuntimeException("Google API returned an empty embedding");
        }
        logger.info("embedding request completed model={} dimension={} durationMs={}",
                model,
                embedding.size(),
                System.currentTimeMillis() - startedAt);
        return embedding;
    }

    public String createAnswer(String query, List<String> contexts) {
        return createAnswer(query, contexts, chatModel);
    }

    public String createAnswer(String query, List<String> contexts, String requestedChatModel) {
        String model = requestedChatModel == null || requestedChatModel.isBlank()
                ? chatModel
                : requestedChatModel;
        long startedAt = System.currentTimeMillis();
        logger.info("answer generation request started model={} queryLength={} contextCount={} contextChars={}",
                model,
                query == null ? 0 : query.length(),
                contexts.size(),
                totalContextChars(contexts));
        for (int i = 0; i < contexts.size(); i++) {
            logger.debug("answer context excerpt index={} length={}", i + 1, lengthOf(contexts.get(i)));
        }
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a retrieval QA assistant. Answer the user question using only the provided document excerpts.\n");
        prompt.append("Rules:\n");
        prompt.append("- If the excerpts contain the answer, answer directly and cite the excerpt numbers like [1].\n");
        prompt.append("- Do not repeat the user question as the answer.\n");
        prompt.append("- If the excerpts do not contain the answer, say exactly: I could not find the answer in the uploaded documents.\n\n");
        prompt.append("Context excerpts:\n");
        for (int i = 0; i < contexts.size(); i++) {
            prompt.append("[" + (i + 1) + "] ").append(contexts.get(i)).append("\n\n");
        }
        prompt.append("User question:\n");
        prompt.append(query).append("\n\n");
        prompt.append("Answer:\n");

        logger.debug("Final prompt length: {} chars", prompt.length());
        
        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt.toString())))));
        request.put("generationConfig", Map.of("temperature", 0, "candidateCount", 1));

        logger.info("gemini generateContent started model={} promptLength={}", model, prompt.length());
        Map<String, Object> response = post(apiUrl + "/v1beta/models/" + modelPathName(model) + ":generateContent", request);
        String answer = parseChatResponse(response);
        logger.info("answer generation request completed model={} answerLength={} durationMs={}",
                model,
                answer != null ? answer.length() : 0,
                System.currentTimeMillis() - startedAt);
        return answer;
    }

    private List<Double> parseEmbedding(Map<String, Object> response) {
        Object embeddingObject = response.get("embedding");
        if (embeddingObject instanceof Map<?, ?> embedding) {
            return parseEmbeddingValues(embedding);
        }

        Object embeddingsObject = response.get("embeddings");
        if (embeddingsObject instanceof List<?> embeddings && !embeddings.isEmpty()
                && embeddings.get(0) instanceof Map<?, ?> embedding) {
            return parseEmbeddingValues(embedding);
        }

        return List.of();
    }

    private List<Double> parseEmbeddingValues(Map<?, ?> embedding) {
        Object valuesObject = embedding.get("values");
        if (!(valuesObject instanceof List<?> values)) {
            return List.of();
        }
        List<Double> result = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Number number) {
                result.add(number.doubleValue());
            }
        }
        return result;
    }

    private String parseChatResponse(Map<String, Object> response) {
        Object candidatesObject = response.get("candidates");
        if (candidatesObject instanceof List<?> candidates && !candidates.isEmpty()) {
            logger.debug("Chat response contains {} candidates", candidates.size());
            Object firstCandidateObject = candidates.get(0);
            if (firstCandidateObject instanceof Map<?, ?> firstCandidate) {
                Object contentObject = firstCandidate.get("content");
                if (contentObject instanceof Map<?, ?> content) {
                    Object partsObject = content.get("parts");
                    if (partsObject instanceof List<?> parts && !parts.isEmpty()
                            && parts.get(0) instanceof Map<?, ?> firstPart) {
                        Object text = firstPart.get("text");
                        if (text instanceof String answer) {
                            return answer;
                        }
                    }
                }
            }
        }
        logger.warn("No valid response received from chat API");
        return "I could not generate an answer.";
    }

    private Map<String, Object> post(String url, Map<String, Object> payload) {
        logger.debug("Making POST request to: {}", url);
        
        if (apiKey == null || apiKey.isEmpty() || "test-key".equals(apiKey)) {
            logger.error("Google API key is not set or invalid");
            throw new RuntimeException("Google API key is not configured. Please set the GOOGLE_API_KEY environment variable with a valid key.");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        String urlWithKey = url + (url.contains("?") ? "&" : "?") + "key=" + apiKey;
        long startedAt = System.currentTimeMillis();
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            logger.debug("google api request completed status={} durationMs={}",
                    response.getStatusCode(),
                    System.currentTimeMillis() - startedAt);
            if (response.getBody() == null) {
                logger.error("API returned null response");
                throw new RuntimeException("Google API returned an empty response");
            }
            
            // Check for Google API error response
            if (response.getBody().containsKey("error")) {
                Object error = response.getBody().get("error");
                logger.error("Google API error: {}", error);
                throw new RuntimeException("Google API error: " + error);
            }
            
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            logger.error("Google API HTTP error: {} {} durationMs={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    System.currentTimeMillis() - startedAt,
                    ex);
            throw new RuntimeException("Failed to call Google API: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
        } catch (org.springframework.web.client.RestClientException ex) {
            logger.error("Error calling Google API (HTTP error): {} durationMs={}",
                    ex.getMessage(),
                    System.currentTimeMillis() - startedAt,
                    ex);
            throw new RuntimeException("Failed to call Google API: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error("Error calling Google API: {} durationMs={}",
                    ex.getMessage(),
                    System.currentTimeMillis() - startedAt,
                    ex);
            throw new RuntimeException("Failed to call Google API: " + ex.getMessage(), ex);
        }
    }

    private int totalContextChars(List<String> contexts) {
        int total = 0;
        for (String context : contexts) {
            total += lengthOf(context);
        }
        return total;
    }

    private int lengthOf(String value) {
        return value == null ? 0 : value.length();
    }

    private String modelPathName(String model) {
        return model.startsWith("models/") ? model.substring("models/".length()) : model;
    }

    private String modelResourceName(String model) {
        return model.startsWith("models/") ? model : "models/" + model;
    }
}
