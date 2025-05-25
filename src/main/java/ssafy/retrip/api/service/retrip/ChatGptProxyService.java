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

  /**
   * ChatGPT 중계서버에 요청을 보내 여행 이미지 분석 결과를 받아오는 메서드
   *
   * @param memberId 회원 ID
   * @param retripId Retrip ID
   * @return 이미지 분석 결과
   */
  public AnalysisResponse getImageAnalysis(String memberId, Long retripId) {
    log.info("ChatGPT 중계서버 요청 시작: memberId={}, retripId={}", memberId, retripId);

    // 요청 본문 데이터 설정
    ImageAnalysisRequest requestBody = new ImageAnalysisRequest(memberId, retripId);

    try {
      // 응답 디버깅을 위해 먼저 문자열로 받습니다
      String responseString = restClient.post()
          .uri(proxyUrl)
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(String.class);

      log.info("중계서버 응답: {}", responseString);

      // 응답이 정상적으로 수신되었으면 객체로 변환
      if (responseString != null) {
        // Jackson ObjectMapper를 사용하여 수동 변환
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
