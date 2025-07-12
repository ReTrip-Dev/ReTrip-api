package ssafy.retrip.utils;

import static lombok.AccessLevel.PRIVATE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@NoArgsConstructor(access = PRIVATE)
public class DateUtil {

  public static LocalDateTime findLatestTakenDate(List<ImageMetaData> metadataList) {
    return metadataList.stream()
        .map(ImageMetaData::getTakenDate)
        .filter(Objects::nonNull)
        .reduce((first, second) -> second)
        .orElse(LocalDateTime.now());
  }

  public static LocalDateTime findEarliestTakenDate(List<ImageMetaData> metadataList) {
    return metadataList.stream()
        .map(ImageMetaData::getTakenDate)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(LocalDateTime.now());
  }
}