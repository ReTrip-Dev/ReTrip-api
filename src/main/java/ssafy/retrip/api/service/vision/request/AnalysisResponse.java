package ssafy.retrip.api.service.vision.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnalysisResponse {

  private List<FailedImageInfo> failedImagesInfo;
  private TravelImageAnalysis travelImageAnalysis;

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class FailedImageInfo {

    private String id;
    private String reason;
  }

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TravelImageAnalysis {

    private TravelAnalysis travelAnalysis;
  }

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TravelAnalysis {

    private String mbti;
    private String overallMood;
    private String personMood;
    private Map<String, String> photoCategoryRatio;
    private List<TopSubject> top5Subjects;
    private List<String> topRecommendPlace;
    private TopVisitPlace topVisitPlace;
  }

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TopSubject {

    private int count;
    private String subject;
  }

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TopVisitPlace {

    // 실제 JSON 응답과 일치하도록 필드명 수정
    private Double latitude;
    private Double longitude;
    private String placeName;
  }
}
