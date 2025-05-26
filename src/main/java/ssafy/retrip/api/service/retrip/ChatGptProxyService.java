package ssafy.retrip.api.service.retrip;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ssafy.retrip.api.service.retrip.request.ImageAnalysisRequest;
import ssafy.retrip.api.service.vision.request.AnalysisResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptProxyService {

  private final RestClient restClient;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${chatgpt.proxy.url}")
  private String proxyUrl;

  public AnalysisResponse getImageAnalysis(ImageAnalysisRequest requestBody) {

    log.info("memberId: {}, retripId: {}, mainLocationLat: {}, mainLocationLng: {}",
        requestBody.getMemberId(), requestBody.getRetripId(),
        requestBody.getMainLocationLat(), requestBody.getMainLocationLng());

    log.info("ChatGPT 중계서버 요청 시작");

    try {
      String responseString = restClient.post()
          .uri(proxyUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(String.class);

      log.info("중계서버 응답: {}", responseString);

      if (responseString != null) {
        AnalysisResponse response = objectMapper.readValue(responseString, AnalysisResponse.class);
        log.info("ChatGPT 중계서버 응답 수신 성공");
        return response;
      }

      return null;
    } catch (Exception e) {
      log.error("ChatGPT 중계서버 통신 중 오류 발생: {}", e.getMessage(), e);
      // 오류 발생 시 null 반환
      return null;
    }
  }
}
