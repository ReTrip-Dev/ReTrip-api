package ssafy.retrip.api.service.vision;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import ssafy.retrip.api.service.vision.request.AnalysisResponse;

@RestClientTest(VisionAnalysisService.class)
@ActiveProfiles("test")
class VisionAnalysisServiceTest {

  private final String AWS_S3_PATH = "s3://retrip-photos-ssafy04/retrip/test/";
  private final String FLASK_API_URL = "http://127.0.0.1:5000/analyze_s3_images";

  private final RestClient restClient = RestClient.create();

  @Test
  @DisplayName("Flask API에 S3 URL을 전송하고 응답을 받는다.")
  void testFlaskApiConnection() {

    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("s3_folder_url", AWS_S3_PATH);

    AnalysisResponse response = restClient.post()
        .uri(FLASK_API_URL)
        .contentType(MediaType.APPLICATION_JSON) // JSON 형식으로 데이터를 보낸다고 명시
        .body(requestBody) // 준비된 요청 본문 객체 설정
        .retrieve() // 응답 검색 시작
        .body(AnalysisResponse.class); // 응답 본문을 AnalysisResponse DTO로 변환

    System.out.println(
        "response : " + response.getTravelImageAnalysis().getTravelAnalysis().getMbti());
    System.out.println(
        "response : " + response.getTravelImageAnalysis().getTravelAnalysis().getOverallMood());
    System.out.println(
        "response : " + response.getTravelImageAnalysis().getTravelAnalysis().getTopVisitPlace());
    System.out.println(
        "response : " + response.getTravelImageAnalysis().getTravelAnalysis().getTop5Subjects());
    System.out.println("response : " + response.getTravelImageAnalysis().getTravelAnalysis()
        .getPhotoCategoryRatio());
    System.out.println(
        "response : " + response.getTravelImageAnalysis().getTravelAnalysis().getClass());

    assertNotNull(response, "Flask API 응답이 null입니다.");

    System.out.println("Flask API 응답: " + response);
  }
}