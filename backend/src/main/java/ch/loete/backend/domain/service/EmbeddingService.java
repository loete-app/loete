package ch.loete.backend.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class EmbeddingService {

  private final RestClient restClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final String apiKey;
  private final String model;

  public EmbeddingService(
      @Value("${app.embeddings.base-url}") String baseUrl,
      @Value("${app.embeddings.api-key}") String apiKey,
      @Value("${app.embeddings.model}") String model) {
    this.apiKey = apiKey;
    this.model = model;
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  public boolean isConfigured() {
    return StringUtils.hasText(apiKey);
  }

  public List<float[]> generateEmbeddings(List<String> texts) {
    if (!isConfigured()) {
      log.warn("Embeddings API key not configured, skipping embedding generation");
      return List.of();
    }

    String responseBody =
        restClient
            .post()
            .uri("/embeddings")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + apiKey)
            .body(Map.of("model", model, "input", texts))
            .retrieve()
            .body(String.class);

    JsonNode response;
    try {
      response = objectMapper.readTree(responseBody);
    } catch (Exception e) {
      log.error("Failed to parse embeddings response: {}", e.getMessage());
      return List.of();
    }

    if (response == null || !response.has("data")) {
      log.error("Invalid response from embeddings API");
      return List.of();
    }

    List<float[]> embeddings = new ArrayList<>();
    for (JsonNode item : response.get("data")) {
      JsonNode embedding = item.get("embedding");
      float[] vector = new float[embedding.size()];
      for (int i = 0; i < embedding.size(); i++) {
        vector[i] = (float) embedding.get(i).asDouble();
      }
      embeddings.add(vector);
    }

    return embeddings;
  }

  public float[] generateEmbedding(String text) {
    List<float[]> result = generateEmbeddings(List.of(text));
    if (result.isEmpty()) {
      throw new IllegalStateException(
          "Failed to generate embedding — check embeddings API key configuration");
    }
    return result.getFirst();
  }

  public static String toVectorString(float[] vector) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < vector.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(vector[i]);
    }
    sb.append("]");
    return sb.toString();
  }
}
