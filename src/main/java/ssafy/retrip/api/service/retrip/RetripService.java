package ssafy.retrip.api.service.retrip;

import static ssafy.retrip.utils.CoordinateUtil.analyzeMainLocation;
import static ssafy.retrip.utils.CoordinateUtil.calculateAverageCoordinates;
import static ssafy.retrip.utils.DateUtil.findEarliestTakenDate;
import static ssafy.retrip.utils.DateUtil.findLatestTakenDate;
import static ssafy.retrip.utils.DistanceUtil.calculateTotalDistance;
import static ssafy.retrip.utils.ImageMetaDataUtil.extractMetadata;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
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
import ssafy.retrip.api.service.retrip.info.GpsCoordinate;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;
import ssafy.retrip.domain.place.RecommendationPlace;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.RetripRepository;
import ssafy.retrip.domain.retrip.TimeSlot;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {

  public static final String DEFAULT_USERNAME = "여행자님";

  private final RetripRepository retripRepository;
  private final ImageConverter imageConverter;
  private final GptImageAnalysisService gptImageAnalysisService;

  @Value("${retrip.image.min-count}")
  private int minImageCount;

  @Value("${retrip.image.max-count}")
  private int maxImageCount;

  @Transactional
  public TravelAnalysisResponseDto createRetripFromImages(List<MultipartFile> images) {

    validateMinimumImageCount(images);

    images = adjustImageCountToMaximum(images);
    List<String> imageDataUrlsForGpt = new ArrayList<>();
    List<ImageMetaData> allMetadata = new ArrayList<>();

    prepareImageForProcessing(images, allMetadata, imageDataUrlsForGpt);

    GpsCoordinate averageCoords = calculateAverageCoordinates(allMetadata);
    AnalysisResponse analysisResponse = gptImageAnalysisService
        .analyze(imageDataUrlsForGpt, averageCoords.latitude(), averageCoords.longitude());

    try {
      Retrip retrip = buildRetripFromAnalysis(analysisResponse);
      updateRetripDetailsFromMetadata(retrip, allMetadata);
      Retrip savedRetrip = retripRepository.save(retrip);

      return buildTravelAnalysisResponseDto(savedRetrip, analysisResponse);
    } catch (Exception e) {
      log.error("GPT 분석 결과 처리 및 Retrip 생성 중 오류 발생", e);
      throw new IllegalStateException("여행 기록 생성 중 오류가 발생했습니다.", e);
    }
  }

  private void validateMinimumImageCount(List<MultipartFile> images) {
    if (images == null || images.size() < minImageCount) {
      throw new IllegalArgumentException("이미지는 최소 " + minImageCount + "장 이상 업로드해야 합니다.");
    }
  }

  private List<MultipartFile> adjustImageCountToMaximum(List<MultipartFile> images) {
    return images.size() <= maxImageCount ? images : images.subList(0, maxImageCount);
  }

  private void prepareImageForProcessing(
      List<MultipartFile> images,
      List<ImageMetaData> allMetadata,
      List<String> imageDataUrlsForGpt
  ) {
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

    if (allMetadata.size() < minImageCount) {
      throw new IllegalStateException("유효한 이미지가 " + minImageCount + "장 미만입니다.");
    }
  }

  private Retrip buildRetripFromAnalysis(AnalysisResponse analysis) {
    AnalysisResponse.User user = analysis.getUser();
    AnalysisResponse.TripSummary summary = analysis.getTripSummary();
    AnalysisResponse.PhotoStats stats = analysis.getPhotoStats();

    if (user == null || summary == null || stats == null) {
      throw new IllegalStateException("GPT 분석 결과의 세부 정보(사용자, 요약, 통계)가 누락되었습니다.");
    }

    AnalysisResponse.EgenTeto egenTeto = user.getEgenTeto();
    String egenTetoType = egenTeto != null ? egenTeto.getType() : null;
    String egenTetoSubtype = egenTeto != null ? egenTeto.getSubtype() : null;
    String egenTetoHashtag = egenTeto != null ? egenTeto.getHashtag() : null;

    Retrip retrip = Retrip.builder()
        .countryCode(user.getCountryCode())
        .mbti(user.getMbti())
        .egenTetoType(egenTetoType)
        .egenTetoSubtype(egenTetoSubtype)
        .egenTetoHashtag(egenTetoHashtag)
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

  private TravelAnalysisResponseDto buildTravelAnalysisResponseDto(
      Retrip retrip,
      AnalysisResponse analysis
  ) {
    return TravelAnalysisResponseDto.from(
        retrip.getId(),
        analysis,
        retrip,
        DEFAULT_USERNAME
    );
  }

  private TimeSlot analyzeMainTimeSlot(List<ImageMetaData> metadataList) {
    if (metadataList.stream().allMatch(m -> m.getTakenDate() == null)) {
      return TimeSlot.AFTERNOON;
    }
    return metadataList.stream()
        .map(ImageMetaData::getTakenDate)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(TimeSlot::from, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(TimeSlot.AFTERNOON);
  }

  public void updateRetripDetailsFromMetadata(Retrip retrip, List<ImageMetaData> metadataList) {
    if (metadataList == null || metadataList.isEmpty()) {
      return;
    }
    metadataList.sort(Comparator.comparing(ImageMetaData::getTakenDate,
        Comparator.nullsLast(Comparator.naturalOrder())));

    LocalDateTime startDate = findEarliestTakenDate(metadataList);
    LocalDateTime endDate = findLatestTakenDate(metadataList);
    Map<String, Object> mainLocationInfo = analyzeMainLocation(metadataList);

    retrip.updateRetripDetailsData(
        startDate,
        endDate,
        metadataList.size(),
        calculateTotalDistance(metadataList),
        analyzeMainTimeSlot(metadataList),
        (Double) mainLocationInfo.get("latitude"),
        (Double) mainLocationInfo.get("longitude")
    );
  }
}