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
    private Map<String, String> photoCategoryRatio;
    private List<TopSubject> top5Subjects;
    private String topVisitPlace;
  }

  @Getter
  @NoArgsConstructor
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public static class TopSubject {
    private int count;
    private String subject;
  }
}
