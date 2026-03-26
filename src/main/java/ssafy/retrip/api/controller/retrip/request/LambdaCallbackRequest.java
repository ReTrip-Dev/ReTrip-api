package ssafy.retrip.api.controller.retrip.request;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.openai.response.AnalysisResponse;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LambdaCallbackRequest {

  private AnalysisResponse analysisResponse;
  private List<MetadataDto> metadata;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetadataDto {

    private LocalDateTime takenDate;
    private Double latitude;
    private Double longitude;

    public ImageMetaData toImageMetaData() {
      ImageMetaData meta = new ImageMetaData();
      meta.updateTakenDate(this.takenDate);
      if (this.latitude != null && this.longitude != null) {
        meta.updateGeoLocation(this.latitude, this.longitude);
      }
      return meta;
    }
  }

  public List<ImageMetaData> toImageMetaDataList() {
    if (metadata == null) {
      return List.of();
    }
    return metadata.stream()
        .map(MetadataDto::toImageMetaData)
        .toList();
  }
}
