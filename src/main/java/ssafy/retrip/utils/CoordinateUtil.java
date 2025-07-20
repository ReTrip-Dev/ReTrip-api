package ssafy.retrip.utils;

import static lombok.AccessLevel.PRIVATE;
import static ssafy.retrip.utils.DistanceUtil.calculateDistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.retrip.api.service.retrip.info.GpsCoordinate;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class CoordinateUtil {

  public static final double SEARCH_RADIUS_KM = 0.1;
  public static final double DEFAULT_LATITUDE = 0.0;
  public static final double DEFAULT_LONGITUDE = 0.0;

  public static GpsCoordinate calculateAverageCoordinates(List<ImageMetaData> metadataList) {

    List<GpsCoordinate> validCoords = metadataList.stream()
        .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
        .map(m -> new GpsCoordinate(m.getLatitude(), m.getLongitude()))
        .toList();

    if (validCoords.isEmpty()) {
      log.warn("유효한 GPS 좌표가 없어 기본값(0, 0)을 사용합니다.");
      return new GpsCoordinate(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
    }

    double avgLat = validCoords.stream()
        .mapToDouble(GpsCoordinate::latitude)
        .average()
        .orElse(DEFAULT_LATITUDE);

    double avgLng = validCoords.stream()
        .mapToDouble(GpsCoordinate::longitude)
        .average()
        .orElse(DEFAULT_LONGITUDE);

    return new GpsCoordinate(avgLat, avgLng);
  }

  public static Map<String, Object> analyzeMainLocation(List<ImageMetaData> metadataList) {

    List<ImageMetaData> locData = metadataList.stream()
        .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
        .toList();

    if (locData.isEmpty()) {
      return Map.of("latitude", DEFAULT_LATITUDE, "longitude", DEFAULT_LONGITUDE);
    }

    List<ImageMetaData> largestCluster = findLargestCluster(locData);
    double avgLat = largestCluster.stream()
        .mapToDouble(ImageMetaData::getLatitude)
        .average()
        .orElse(DEFAULT_LATITUDE);

    double avgLng = largestCluster.stream()
        .mapToDouble(ImageMetaData::getLongitude)
        .average()
        .orElse(DEFAULT_LONGITUDE);

    return Map.of("latitude", avgLat, "longitude", avgLng);
  }

  private static List<ImageMetaData> findLargestCluster(List<ImageMetaData> metadataList) {
    if (metadataList.isEmpty()) {
      return Collections.emptyList();
    }
    List<List<ImageMetaData>> clusters = new ArrayList<>();
    for (ImageMetaData meta : metadataList) {
      boolean foundCluster = false;
      for (List<ImageMetaData> cluster : clusters) {
        double dist = calculateDistance(meta.getLatitude(), meta.getLongitude(),
            cluster.get(0).getLatitude(), cluster.get(0).getLongitude());
        if (dist <= SEARCH_RADIUS_KM) {
          cluster.add(meta);
          foundCluster = true;
          break;
        }
      }
      if (!foundCluster) {
        clusters.add(new ArrayList<>(Collections.singletonList(meta)));
      }
    }

    return clusters.stream()
        .max(Comparator.comparingInt(List::size))
        .orElse(Collections.emptyList());
  }
}