package ssafy.retrip.api.service.retrip.info;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageMetaData {

  private LocalDateTime takenDate;
  private Double latitude;
  private Double longitude;

  public void updateTakenDate(LocalDateTime takenDate) {
    this.takenDate = takenDate;
  }

  public void updateGeoLocation(Double latitude, Double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
