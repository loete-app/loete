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

/**
 * Service für die Generierung von Vektor-Embeddings über eine externe API.
 *
 * <p>Kommuniziert mit einer OpenAI-kompatiblen Embeddings-API, um Texte in Vektoren umzuwandeln.
 * Diese Vektoren werden für die semantische Vibe-Suche in pgvector gespeichert.
 */
@Slf4j
@Service
public class EmbeddingService {

  /** HTTP-Client für die Kommunikation mit der Embeddings-API. */
  private final RestClient restClient;

  /** Jackson ObjectMapper für das Parsen der API-Antworten. */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** API-Schlüssel für die Embeddings-API. */
  private final String apiKey;

  /** Name des zu verwendenden Embedding-Modells. */
  private final String model;

  /**
   * Erstellt einen neuen EmbeddingService.
   *
   * @param baseUrl die Basis-URL der Embeddings-API
   * @param apiKey der API-Schlüssel
   * @param model der Modellname (z.B. "text-embedding-3-small")
   */
  public EmbeddingService(
      @Value("${app.embeddings.base-url}") String baseUrl,
      @Value("${app.embeddings.api-key}") String apiKey,
      @Value("${app.embeddings.model}") String model) {
    this.apiKey = apiKey;
    this.model = model;
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  /**
   * Prüft, ob die Embeddings-API konfiguriert ist (API-Key vorhanden).
   *
   * @return {@code true} wenn ein API-Key gesetzt ist
   */
  public boolean isConfigured() {
    return StringUtils.hasText(apiKey);
  }

  /**
   * Generiert Embeddings für eine Liste von Texten.
   *
   * @param texts die zu vektorisierenden Texte
   * @return Liste der Embedding-Vektoren, leer bei Fehler oder fehlender Konfiguration
   */
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

  /**
   * Generiert ein einzelnes Embedding für einen Text.
   *
   * @param text der zu vektorisierende Text
   * @return der Embedding-Vektor
   * @throws IllegalStateException wenn die Generierung fehlschlaegt
   */
  public float[] generateEmbedding(String text) {
    List<float[]> result = generateEmbeddings(List.of(text));
    if (result.isEmpty()) {
      throw new IllegalStateException(
          "Failed to generate embedding — check embeddings API key configuration");
    }
    return result.getFirst();
  }

  /**
   * Konvertiert einen float-Vektor in das pgvector-String-Format.
   *
   * @param vector der zu konvertierende Vektor
   * @return der Vektor als String im Format "[0.1,0.2,...]"
   */
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
