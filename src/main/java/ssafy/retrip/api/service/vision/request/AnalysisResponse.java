package ssafy.retrip.api.service.vision.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
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
  public static class FailedImageInfo {
    private String id;
    private String reason;
  }

  @Getter
  @NoArgsConstructor
  public static class TravelImageAnalysis {
    private User user;
    private TripSummary tripSummary;
    private PhotoStats photoStats;
    private List<Recommendation> recommendations;
  }

  @Getter
  @NoArgsConstructor
  public static class User {
    private String countryCode;
    private String mbti;
  }

  @Getter
  @NoArgsConstructor
  public static class TripSummary {
    private String summaryLine;
    private List<String> keywords;
    private String hashtag;
  }

  @Getter
  @NoArgsConstructor
  public static class PhotoStats {
    private List<String> favoriteSubjects;
    private String favoritePhotoSpot;
  }

  @Getter
  @NoArgsConstructor
  public static class Recommendation {
    private String emoji;
    private String place;
    private String description;
  }
}