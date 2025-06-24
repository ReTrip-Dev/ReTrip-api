package ssafy.retrip.api.service.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ssafy.retrip.api.service.openai.response.AnalysisResponse;

/**
 * OpenAI의 GPT 모델을 사용하여 이미지 분석을 수행하는 서비스입니다. 이미지 데이터와 위치 정보를 기반으로 프롬프트를 생성하고, GPT API를 호출하여 분석 결과를
 * 받아옵니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GptImageAnalysisService {

  // 프롬프트 템플릿 내 위치 정보 플레이스홀더 상수
  private static final String LATITUDE_PLACEHOLDER = "{main_location_lat}";
  private static final String LONGITUDE_PLACEHOLDER = "{main_location_lng}";

  private final OpenAiClient openAiClient;
  private final ResourceLoader resourceLoader;
  private final ObjectMapper objectMapper;
  private String promptTemplate;

  /**
   * 서비스 초기화 시 클래스패스에서 'analysis.prompt' 파일을 읽어와 프롬프트 템플릿으로 메모리에 로드합니다. 파일 로드에 실패할 경우,
   * RuntimeException을 발생시켜 애플리케이션 시작을 중단합니다.
   *
   * @throws RuntimeException 프롬프트 파일을 찾을 수 없거나 읽는 중 오류가 발생할 경우
   */
  @jakarta.annotation.PostConstruct
  private void init() {
    try {
      Resource resource = resourceLoader.getResource("classpath:analysis.prompt");
      this.promptTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      log.error("analysis.prompt 파일을 읽는 중 오류 발생", e);
      throw new RuntimeException("프롬프트 파일을 로드할 수 없습니다.", e);
    }
  }

  /**
   * 주어진 이미지 데이터와 위치 정보를 사용하여 GPT에 분석을 요청하고, 그 결과를 파싱하여 DTO로 반환합니다.
   *
   * @param imageDataUrls 분석할 이미지의 데이터 URL 목록 (Base64 인코딩)
   * @param latitude      주요 위치의 위도
   * @param longitude     주요 위치의 경도
   * @return GPT가 반환한 분석 결과를 담은 {@link AnalysisResponse} 객체
   * @throws IllegalStateException GPT 응답 JSON을 파싱하는 데 실패할 경우
   */
  public AnalysisResponse analyze(List<String> imageDataUrls, double latitude, double longitude) {
    // 프롬프트 템플릿에 위도, 경도 값 삽입
    String prompt = promptTemplate
        .replace(LATITUDE_PLACEHOLDER, String.format("%.6f", latitude))
        .replace(LONGITUDE_PLACEHOLDER, String.format("%.6f", longitude));

    // OpenAI 클라이언트를 통해 분석 요청
    String gptAnalysisJson = openAiClient.analyzeImages(prompt, imageDataUrls);

    // GPT로부터 받은 원본 JSON 응답을 로그로 기록 (디버깅용)
    log.info("GPT-4o로부터 받은 분석 결과 (JSON): {}", gptAnalysisJson);

    try {
      // 반환된 JSON 문자열을 AnalysisResponse 객체로 변환
      return objectMapper.readValue(gptAnalysisJson, AnalysisResponse.class);
    } catch (Exception e) {
      // JSON 파싱 실패 시, 원본 응답과 함께 에러 로그 기록
      log.error("GPT 응답 JSON 파싱 중 오류 발생. Response: {}", gptAnalysisJson, e);
      throw new IllegalStateException("GPT 분석 결과를 파싱하는 데 실패했습니다.", e);
    }
  }
}