package ssafy.retrip.api.service.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

  /**
   * GPT-4o에 이미지와 프롬프트를 전송하여 분석 결과를 JSON 형태로 반환
   */
  public String analyzeImages(String prompt, List<String> imageDataUrls) {
    try {
      // 메시지 구성
      Map<String, Object> message = Map.of(
          "role", "user",
          "content", buildContent(prompt, imageDataUrls)
      );

      // API 요청 본문 구성
      Map<String, Object> requestBody = Map.of(
          "model", model,
          "messages", List.of(message),
          "max_tokens", 2000,
          "response_format", Map.of("type", "json_object")
      );

      // API 호출
      String response = restClient.post()
          .uri(apiUrl + "/chat/completions")
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(String.class);

      // 응답에서 콘텐츠 추출
      JsonNode jsonResponse = objectMapper.readTree(response);
      return jsonResponse.path("choices").path(0).path("message").path("content").asText();
    } catch (Exception e) {
      log.error("OpenAI API 호출 중 오류 발생", e);
      throw new RuntimeException("이미지 분석 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  /**
   * OpenAI API 요청에 필요한 콘텐츠 구성
   */
  private List<Map<String, Object>> buildContent(String prompt, List<String> imageDataUrls) {
    // 텍스트 프롬프트
    Map<String, Object> textContent = Map.of(
        "type", "text",
        "text", prompt
    );

    // 이미지 콘텐츠 구성
    List<Map<String, Object>> contents = imageDataUrls.stream()
        .map(url -> Map.of(
            "type", "image_url",
            "image_url", Map.of("url", url)
        ))
        .collect(java.util.stream.Collectors.toList());

    // 텍스트 프롬프트를 맨 앞에 추가
    contents.add(0, textContent);
    return contents;
  }
}
