package ssafy.retrip.api.service.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  @Value("${openai.api-key}")
  private String apiKey;

  @Value("${openai.api.url:https://api.openai.com/v1}")
  private String apiUrl;

  @Value("${openai.model}")
  private String model;

  @Async("openAiExecutor")
  public CompletableFuture<String> analyzeImages(String prompt, List<String> imageDataUrls) {
    long startTime = System.currentTimeMillis();
    String threadName = Thread.currentThread().getName();

    log.info("OpenAI API 요청 시작 - Thread: {}, 시작시간: {}",
        threadName, LocalDateTime.now());

    try {
      Map<String, Object> message = Map.of(
          "role", "user",
          "content", buildContent(prompt, imageDataUrls)
      );

      Map<String, Object> requestBody = Map.of(
          "model", model,
          "messages", List.of(message),
          "max_tokens", 2000,
          "response_format", Map.of("type", "json_object")
      );

      String response = restClient.post()
          .uri(apiUrl + "/chat/completions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(String.class);

      JsonNode jsonResponse = objectMapper.readTree(response);
      long endTime = System.currentTimeMillis();
      long executionTime = endTime - startTime;

      log.info("OpenAI API 요청 완료 - Thread: {}, 종료시간: {}, 실행시간: {}ms",
          threadName, LocalDateTime.now(), executionTime);

      String result = jsonResponse.path("choices").path(0).path("message").path("content").asText();

      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      log.error("[{}] GPT 호출 실패", threadName, e);
      return CompletableFuture.failedFuture(
          new RuntimeException("GPT API 호출 중 오류가 발생했습니다: " + e.getMessage(), e)
      );
    }
  }


  private List<Map<String, Object>> buildContent(String prompt, List<String> imageDataUrls) {
    Map<String, Object> textContent = Map.of(
        "type", "text",
        "text", prompt
    );

    List<Map<String, Object>> contents = imageDataUrls.stream()
        .map(url -> Map.of(
            "type", "image_url",
            "image_url", Map.of("url", url)
        ))
        .collect(java.util.stream.Collectors.toList());

    contents.add(0, textContent);
    return contents;
  }
}
