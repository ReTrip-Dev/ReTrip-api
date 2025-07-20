package ssafy.retrip.utils;

import static com.drew.metadata.exif.ExifSubIFDDirectory.*;
import static lombok.AccessLevel.PRIVATE;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class ImageMetaDataUtil {

  public static ImageMetaData extractMetadata(InputStream inputStream) {
    ImageMetaData metadata = new ImageMetaData();
    try {
      Metadata rawMetadata = ImageMetadataReader.readMetadata(inputStream);
      extractDateTimeInfo(rawMetadata, metadata);
      extractGpsInfo(rawMetadata, metadata);
    } catch (Exception e) {
      log.error("메타데이터 추출 오류", e);
    }
    return metadata;
  }

  private static void extractDateTimeInfo(Metadata rawMetadata, ImageMetaData metadata) {
    ExifSubIFDDirectory exifDir = rawMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if (exifDir != null) {
      Date date = exifDir.getDate(TAG_DATETIME_ORIGINAL);
      if (date != null) {
        metadata.updateTakenDate(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
      }
    }
  }

  private static void extractGpsInfo(Metadata rawMetadata, ImageMetaData metadata) {
    GpsDirectory gpsDir = rawMetadata.getFirstDirectoryOfType(GpsDirectory.class);
    if (gpsDir != null) {
      GeoLocation geoLocation = gpsDir.getGeoLocation();
      if (geoLocation != null && !geoLocation.isZero()) {
        metadata.updateGeoLocation(geoLocation.getLatitude(), geoLocation.getLongitude());
      }
    }
  }
}