package ssafy.retrip.api.service.retrip;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.openai.GptImageAnalysisService;
import ssafy.retrip.api.service.openai.response.AnalysisResponse;
import ssafy.retrip.api.service.openai.response.AnalysisResponse.Recommendation;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;
import ssafy.retrip.domain.retrip.RecommendationPlace;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.RetripRepository;
import ssafy.retrip.domain.retrip.TimeSlot;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {

  private final MemberRepository memberRepository;
  private final RetripRepository retripRepository;
  private final ImageConverter imageConverter;
  private final GptImageAnalysisService gptImageAnalysisService;

  @Value("${retrip.image.min-count}")
  private int minImages;
  @Value("${retrip.image.max-count}")
  private int maxImages;

  /**
   * 여러 장의 이미지를 받아 여행 기록(Retrip)을 생성하고 분석 결과를 반환합니다. 이미지 처리, 메타데이터 추출, GPT 분석, DB 저장을 총괄합니다.
   *
   * @param images   사용자가 업로드한 이미지 파일 리스트
   * @param memberId 요청을 보낸 회원의 ID (비회원일 경우 null)
   * @return 생성된 여행 기록 정보와 GPT 분석 결과를 담은 DTO
   */
  @Transactional
  public TravelAnalysisResponseDto createRetripFromImages(List<MultipartFile> images,
      String memberId) {
    validateImageCount(images);

    List<String> imageDataUrlsForGpt = new ArrayList<>();
    List<ImageMetadata> allMetadata = new ArrayList<>();

    for (MultipartFile image : images) {
      if (image.isEmpty()) {
        continue;
      }
      try {
        allMetadata.add(extractMetadata(image.getInputStream()));
        byte[] resizedImageBytes = imageConverter.convertAndResizeToJpeg(image);
        imageDataUrlsForGpt.add(imageConverter.toDataUrl(resizedImageBytes));
      } catch (Exception e) {
        log.error("이미지 처리 중 오류 발생: {}", image.getOriginalFilename(), e);
      }
    }

    if (allMetadata.size() < minImages) {
      throw new IllegalStateException("유효한 이미지가 " + minImages + "장 미만입니다.");
    }

    GpsCoordinates averageCoords = calculateAverageCoordinates(allMetadata);
    AnalysisResponse analysisResponse = gptImageAnalysisService.analyze(
        imageDataUrlsForGpt, averageCoords.getLatitude(), averageCoords.getLongitude());

    try {
      Member member = null;
      // memberId가 제공된 경우에만 회원 정보를 조회합니다.
      if (memberId != null && !memberId.trim().isEmpty()) {
        member = memberRepository.findByKakaoId(memberId)
            .orElseThrow(
                () -> new IllegalArgumentException("해당 ID를 가진 회원이 존재하지 않습니다: " + memberId));
      }

      Retrip retrip = buildRetripFromAnalysis(member, analysisResponse);
      updateRetripDetailsFromMetadata(retrip, allMetadata);
      Retrip savedRetrip = retripRepository.save(retrip);

      return buildTravelAnalysisResponseDto(savedRetrip, analysisResponse);
    } catch (Exception e) {
      log.error("GPT 분석 결과 처리 및 Retrip 생성 중 오류 발생", e);
      throw new IllegalStateException("여행 기록 생성 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * GPT 분석 결과를 바탕으로 Retrip 엔티티의 기본 정보를 빌드합니다.
   *
   * @param member   Retrip의 소유자
   * @param analysis GPT로부터 받은 분석 결과 객체
   * @return GPT 분석 정보가 채워진 Retrip 엔티티
   */
  private Retrip buildRetripFromAnalysis(Member member, AnalysisResponse analysis) {
    AnalysisResponse.User user = analysis.getUser();
    AnalysisResponse.TripSummary summary = analysis.getTripSummary();
    AnalysisResponse.PhotoStats stats = analysis.getPhotoStats();

    if (user == null || summary == null || stats == null) {
      throw new IllegalStateException("GPT 분석 결과의 세부 정보(사용자, 요약, 통계)가 누락되었습니다.");
    }

    Retrip retrip = Retrip.builder()
        .member(member)
        .countryCode(user.getCountryCode())
        .mbti(user.getMbti())
        .summaryLine(summary.getSummaryLine())
        .keywords(String.join(",", summary.getKeywords()))
        .hashtag(summary.getHashtag())
        .favoriteSubjects(String.join(",", stats.getFavoriteSubjects()))
        .favoritePhotoSpot(stats.getFavoritePhotoSpot())
        .build();

    List<AnalysisResponse.Recommendation> recommendationDtos = analysis.getRecommendations();
    if (recommendationDtos != null) {
      for (Recommendation dto : recommendationDtos) {
        retrip.addRecommendation(RecommendationPlace.builder()
            .emoji(dto.getEmoji())
            .place(dto.getPlace())
            .description(dto.getDescription())
            .build());
      }
    }
    return retrip;
  }

  /**
   * 이미지 메타데이터 리스트를 분석하여 Retrip 엔티티의 통계 정보를 업데이트합니다. (여행 시작/종료일, 총 이동 거리, 주요 시간대, 주요 위치 좌표 등)
   *
   * @param retrip       정보를 업데이트할 Retrip 엔티티
   * @param metadataList 이미지에서 추출한 메타데이터 리스트
   */
  private void updateRetripDetailsFromMetadata(Retrip retrip, List<ImageMetadata> metadataList) {
    if (metadataList == null || metadataList.isEmpty()) {
      return;
    }
    metadataList.sort(Comparator.comparing(ImageMetadata::getTakenDate,
        Comparator.nullsLast(Comparator.naturalOrder())));
    metadataList.stream().map(ImageMetadata::getTakenDate).filter(Objects::nonNull).findFirst()
        .ifPresent(retrip::setStartDate);
    metadataList.stream().map(ImageMetadata::getTakenDate).filter(Objects::nonNull)
        .reduce((first, second) -> second)
        .ifPresent(retrip::setEndDate);
    retrip.setTotalDistance(calculateTotalDistance(metadataList));
    retrip.setMainTimeSlot(analyzeMainTimeSlot(metadataList));
    Map<String, Object> mainLocationInfo = analyzeMainLocation(metadataList);
    retrip.setMainLocationLat((Double) mainLocationInfo.get("latitude"));
    retrip.setMainLocationLng((Double) mainLocationInfo.get("longitude"));
    retrip.setImageCount(metadataList.size());
  }

  /**
   * 저장된 Retrip 엔티티와 GPT 분석 결과를 조합하여 최종적으로 클라이언트에게 반환할 DTO를 생성합니다. 회원이 없는 경우를 처리합니다.
   *
   * @param retrip   DB에 저장된 Retrip 엔티티
   * @param analysis GPT로부터 받은 분석 결과 객체
   * @return 클라이언트에게 전달될 최종 응답 DTO
   */
  private TravelAnalysisResponseDto buildTravelAnalysisResponseDto(Retrip retrip,
      AnalysisResponse analysis) {
    // 회원이 존재하면 회원의 닉네임을, 없으면 "비회원"를 사용합니다.
    String username =
        (retrip.getMember() != null) ? retrip.getMember().getNickname() : "비회원";

    return TravelAnalysisResponseDto.from(
        retrip.getId(),
        analysis,
        retrip,
        username
    );
  }

  /**
   * 업로드된 이미지의 개수가 유효한 범위(최소/최대) 내에 있는지 검증합니다.
   *
   * @param images 업로드된 이미지 파일 리스트
   * @throws IllegalArgumentException 이미지 개수가 최소 요구량보다 적을 경우
   */
  private void validateImageCount(List<MultipartFile> images) {
    if (images == null || images.size() < minImages) {
      throw new IllegalArgumentException("이미지는 최소 " + minImages + "장 이상 업로드해야 합니다.");
    }
    if (images.size() > maxImages) {
      images = images.subList(0, maxImages);
    }
  }

  /**
   * 이미지 메타데이터 리스트로부터 평균 GPS 좌표를 계산합니다.
   *
   * @param metadataList 이미지 메타데이터 리스트
   * @return 평균 위도와 경도를 담은 GpsCoordinates 객체
   */
  private GpsCoordinates calculateAverageCoordinates(List<ImageMetadata> metadataList) {
    List<GpsCoordinates> validCoords = metadataList.stream()
        .filter(m -> m.latitude != null && m.longitude != null)
        .map(m -> new GpsCoordinates(m.latitude, m.longitude))
        .collect(Collectors.toList());
    if (validCoords.isEmpty()) {
      log.warn("유효한 GPS 좌표가 없어 기본값(0,0)을 사용합니다.");
      return new GpsCoordinates(0.0, 0.0);
    }
    double avgLat = validCoords.stream().mapToDouble(GpsCoordinates::getLatitude).average()
        .orElse(0.0);
    double avgLng = validCoords.stream().mapToDouble(GpsCoordinates::getLongitude).average()
        .orElse(0.0);
    return new GpsCoordinates(avgLat, avgLng);
  }

  /**
   * 이미지 파일의 InputStream에서 촬영 시간, GPS 정보 등의 메타데이터를 추출합니다.
   *
   * @param inputStream 이미지 파일의 InputStream
   * @return 추출된 메타데이터를 담은 ImageMetadata 객체
   */
  private ImageMetadata extractMetadata(InputStream inputStream) {
    ImageMetadata metadata = new ImageMetadata();
    try {
      Metadata rawMetadata = ImageMetadataReader.readMetadata(inputStream);
      extractDateTimeInfo(rawMetadata, metadata);
      extractGpsInfo(rawMetadata, metadata);
    } catch (Exception e) {
      log.error("메타데이터 추출 오류", e);
    }
    return metadata;
  }

  /**
   * EXIF 데이터에서 원본 촬영 시간을 추출하여 ImageMetadata 객체에 설정합니다.
   *
   * @param rawMetadata 원본 메타데이터 객체
   * @param metadata    정보를 저장할 ImageMetadata 객체
   */
  private void extractDateTimeInfo(Metadata rawMetadata, ImageMetadata metadata) {
    ExifSubIFDDirectory exifDir = rawMetadata.getFirstDirectoryOfType(
        ExifSubIFDDirectory.class);
    if (exifDir != null) {
      Date date = exifDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
      if (date != null) {
        metadata.takenDate = LocalDateTime.ofInstant(date.toInstant(),
            ZoneId.systemDefault());
      }
    }
  }

  /**
   * EXIF 데이터에서 GPS 좌표를 추출하여 ImageMetadata 객체에 설정합니다.
   *
   * @param rawMetadata 원본 메타데이터 객체
   * @param metadata    정보를 저장할 ImageMetadata 객체
   */
  private void extractGpsInfo(Metadata rawMetadata, ImageMetadata metadata) {
    GpsDirectory gpsDir = rawMetadata.getFirstDirectoryOfType(GpsDirectory.class);
    if (gpsDir != null) {
      GeoLocation geoLocation = gpsDir.getGeoLocation();
      if (geoLocation != null && !geoLocation.isZero()) {
        metadata.latitude = geoLocation.getLatitude();
        metadata.longitude = geoLocation.getLongitude();
      }
    }
  }

  /**
   * GPS 좌표가 있는 사진들의 촬영 순서에 따라 총 이동 거리를 계산합니다. (단위: km)
   *
   * @param metadataList 이미지 메타데이터 리스트
   * @return 계산된 총 이동 거리 (km)
   */
  private double calculateTotalDistance(List<ImageMetadata> metadataList) {
    double totalDistance = 0.0;
    List<ImageMetadata> locData = metadataList.stream()
        .filter(m -> m.latitude != null && m.longitude != null)
        .collect(Collectors.toList());
    for (int i = 0; i < locData.size() - 1; i++) {
      ImageMetadata current = locData.get(i);
      ImageMetadata next = locData.get(i + 1);
      totalDistance += calculateDistance(current.getLatitude(), current.getLongitude(),
          next.getLatitude(), next.getLongitude());
    }
    return totalDistance;
  }

  /**
   * 두 GPS 좌표 간의 거리를 Haversine 공식을 사용하여 계산합니다. (단위: km)
   *
   * @param lat1 첫 번째 지점의 위도
   * @param lon1 첫 번째 지점의 경도
   * @param lat2 두 번째 지점의 위도
   * @param lon2 두 번째 지점의 경도
   * @return 두 지점 간의 거리 (km)
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371; // 지구의 반지름 (km)
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  /**
   * 사진들의 촬영 시간을 분석하여 가장 많이 촬영된 시간대(아침, 오후 등)를 결정합니다.
   *
   * @param metadataList 이미지 메타데이터 리스트
   * @return 가장 빈도가 높은 TimeSlot
   */
  private TimeSlot analyzeMainTimeSlot(List<ImageMetadata> metadataList) {
    if (metadataList.stream().allMatch(m -> m.getTakenDate() == null)) {
      return TimeSlot.AFTERNOON; // 기본값
    }
    return metadataList.stream()
        .map(ImageMetadata::getTakenDate)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(TimeSlot::from, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(TimeSlot.AFTERNOON);
  }

  /**
   * 사진들의 GPS 정보를 클러스터링하여 가장 큰 클러스터의 평균 좌표를 계산합니다. 이는 여행의 주요 활동 지역을 나타냅니다.
   *
   * @param metadataList 이미지 메타데이터 리스트
   * @return 주요 위치의 위도와 경도를 담은 Map
   */
  private Map<String, Object> analyzeMainLocation(List<ImageMetadata> metadataList) {
    List<ImageMetadata> locData = metadataList.stream()
        .filter(m -> m.latitude != null && m.longitude != null)
        .collect(Collectors.toList());
    if (locData.isEmpty()) {
      return Map.of("latitude", 0.0, "longitude", 0.0);
    }
    List<ImageMetadata> largestCluster = findLargestCluster(locData, 0.1); // 100m 반경
    double avgLat = largestCluster.stream().mapToDouble(ImageMetadata::getLatitude).average()
        .orElse(0.0);
    double avgLng = largestCluster.stream().mapToDouble(ImageMetadata::getLongitude).average()
        .orElse(0.0);
    return Map.of("latitude", avgLat, "longitude", avgLng);
  }

  /**
   * 간단한 클러스터링 알고리즘을 사용하여 주어진 임계값 내에서 가장 큰 사진 그룹을 찾습니다.
   *
   * @param metadataList GPS 정보가 있는 메타데이터 리스트
   * @param thresholdKm  같은 클러스터로 간주할 거리 임계값 (km)
   * @return 가장 큰 클러스터에 속하는 메타데이터 리스트
   */
  private List<ImageMetadata> findLargestCluster(List<ImageMetadata> metadataList,
      double thresholdKm) {
    if (metadataList.isEmpty()) {
      return Collections.emptyList();
    }
    List<List<ImageMetadata>> clusters = new ArrayList<>();
    for (ImageMetadata meta : metadataList) {
      boolean foundCluster = false;
      for (List<ImageMetadata> cluster : clusters) {
        // 클러스터의 첫 번째 요소와 거리를 비교하여 클러스터에 추가할지 결정
        double dist = calculateDistance(meta.getLatitude(), meta.getLongitude(),
            cluster.get(0).getLatitude(), cluster.get(0).getLongitude());
        if (dist <= thresholdKm) {
          cluster.add(meta);
          foundCluster = true;
          break;
        }
      }
      if (!foundCluster) {
        // 어떤 클러스터에도 속하지 않으면 새로운 클러스터 생성
        clusters.add(new ArrayList<>(Collections.singletonList(meta)));
      }
    }
    // 가장 크기가 큰 클러스터 반환
    return clusters.stream().max(Comparator.comparingInt(List::size))
        .orElse(Collections.emptyList());
  }

  /**
   * GPS 좌표(위도, 경도)를 저장하기 위한 내부 정적 클래스입니다.
   */
  @Data
  @AllArgsConstructor
  private static class GpsCoordinates {

    double latitude;
    double longitude;
  }

  /**
   * 이미지에서 추출한 주요 메타데이터(촬영 시간, GPS)를 저장하기 위한 내부 정적 클래스입니다.
   */
  @Data
  private static class ImageMetadata {

    LocalDateTime takenDate;
    Double latitude;
    Double longitude;
  }
}