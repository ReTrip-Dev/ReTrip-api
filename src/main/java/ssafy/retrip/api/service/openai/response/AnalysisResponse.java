package ssafy.retrip.api.service.openai.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnalysisResponse {

  private User user;
  private TripSummary tripSummary;
  private PhotoStats photoStats;
  private List<Recommendation> recommendations;
  private List<FailedImageInfo> failedImages;

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

  @Getter
  @NoArgsConstructor
  public static class FailedImageInfo {

    private int imageIndex;
    private String reason;
  }
}