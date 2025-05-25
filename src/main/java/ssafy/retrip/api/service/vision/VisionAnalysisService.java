package ssafy.retrip.api.service.vision;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ssafy.retrip.api.service.vision.request.AnalysisResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class VisionAnalysisService {

  private final String AWS_S3_PATH = "s3://retrip-photos-ssafy04/retrip/test/";
  private final String FLASK_API_URL = "http://127.0.0.1:5000/analyze_s3_images";

  private final RestClient restClient;

  public void analyzeImage() {

    AnalysisResponse response = restClient.post()
        .uri(FLASK_API_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .body(AWS_S3_PATH)
        .retrieve()
        .body(AnalysisResponse.class);
  }

}
