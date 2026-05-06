package ch.loete.backend.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmbeddingServiceTest {

  @Test
  void isConfigured_returnsFalseWhenKeyBlank() {
    EmbeddingService service = new EmbeddingService("https://api.example.com", "", "model");
    assertThat(service.isConfigured()).isFalse();
  }

  @Test
  void isConfigured_returnsFalseWhenKeyNull() {
    EmbeddingService service = new EmbeddingService("https://api.example.com", null, "model");
    assertThat(service.isConfigured()).isFalse();
  }

  @Test
  void isConfigured_returnsTrueWhenKeyPresent() {
    EmbeddingService service = new EmbeddingService("https://api.example.com", "sk-test", "model");
    assertThat(service.isConfigured()).isTrue();
  }

  @Test
  void generateEmbeddings_returnsEmptyWhenNotConfigured() {
    EmbeddingService service = new EmbeddingService("https://api.example.com", "", "model");
    assertThat(service.generateEmbeddings(java.util.List.of("text"))).isEmpty();
  }

  @Test
  void generateEmbedding_throwsWhenNotConfigured() {
    EmbeddingService service = new EmbeddingService("https://api.example.com", "", "model");
    assertThatThrownBy(() -> service.generateEmbedding("text"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("embeddings API key");
  }

  @Test
  void toVectorString_formatsCorrectly() {
    float[] vector = new float[] {0.1f, 0.2f, 0.3f};
    String result = EmbeddingService.toVectorString(vector);
    assertThat(result).isEqualTo("[0.1,0.2,0.3]");
  }

  @Test
  void toVectorString_handlesSingleElement() {
    float[] vector = new float[] {0.5f};
    String result = EmbeddingService.toVectorString(vector);
    assertThat(result).isEqualTo("[0.5]");
  }

  @Test
  void toVectorString_handlesEmptyArray() {
    float[] vector = new float[] {};
    String result = EmbeddingService.toVectorString(vector);
    assertThat(result).isEqualTo("[]");
  }
}
