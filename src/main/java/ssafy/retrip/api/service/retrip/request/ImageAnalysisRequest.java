package ssafy.retrip.api.service.retrip.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageAnalysisRequest {

  private String memberId;
  private Long retripId;
  private Double mainLocationLat;
  private Double mainLocationLng;

  @Builder
  private ImageAnalysisRequest(String memberId, Long retripId, Double mainLocationLat,
      Double mainLocationLng) {
    this.memberId = memberId;
    this.retripId = retripId;
    this.mainLocationLat = mainLocationLat;
    this.mainLocationLng = mainLocationLng;
  }
}
