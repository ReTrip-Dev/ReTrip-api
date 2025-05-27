package ssafy.retrip.api.service.retrip.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageUrlResponse {

  private String imageUrl;

  @Builder
  private ImageUrlResponse(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}