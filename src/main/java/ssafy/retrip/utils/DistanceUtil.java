package ssafy.retrip.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@NoArgsConstructor(access = PRIVATE)
public class DistanceUtil {

  public static final int EARTH_RADIUS_KM = 6371;

  public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  public static double calculateTotalDistance(List<ImageMetaData> metadataList) {

    double totalDistance = 0.0;
    List<ImageMetaData> locData = metadataList.stream()
        .filter(m -> m.getLatitude() != null && m.getLongitude() != null)
        .toList();

    for (int i = 0; i < locData.size() - 1; i++) {
      ImageMetaData current = locData.get(i);
      ImageMetaData next = locData.get(i + 1);
      totalDistance += calculateDistance(current.getLatitude(), current.getLongitude(),
          next.getLatitude(), next.getLongitude());
    }

    return totalDistance;
  }
}
