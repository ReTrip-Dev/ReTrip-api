package ssafy.retrip.api.service.retrip;

import static ssafy.retrip.domain.retrip.TimeSlot.AFTERNOON;
import static ssafy.retrip.utils.CoordinateUtil.analyzeMainLocation;
import static ssafy.retrip.utils.DateUtil.findEarliestTakenDate;
import static ssafy.retrip.utils.DateUtil.findLatestTakenDate;
import static ssafy.retrip.utils.DistanceUtil.calculateTotalDistance;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.openai.response.AnalysisResponse;
import ssafy.retrip.api.service.openai.response.AnalysisResponse.Recommendation;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;
import ssafy.retrip.domain.place.RecommendationPlace;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.RetripRepository;
import ssafy.retrip.domain.retrip.TimeSlot;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetripPersistenceService {

  public static final String DEFAULT_USERNAME = "여행자님";
  private final RetripRepository retripRepository;

  @Transactional
  public TravelAnalysisResponseDto saveRetrip(AnalysisResponse analysisResponse, List<ImageMetaData> allMetadata) {
    Retrip retrip = buildRetripFromAnalysis(analysisResponse);
    updateRetripDetailsFromMetadata(retrip, allMetadata);
    Retrip savedRetrip = retripRepository.save(retrip);
    return buildTravelAnalysisResponseDto(savedRetrip, analysisResponse);
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
            .build()
        );
      }
    }

    return retrip;
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
      return AFTERNOON;
    }
    return metadataList.stream()
        .map(ImageMetaData::getTakenDate)
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(TimeSlot::from, Collectors.counting()))
        .entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(AFTERNOON);
  }
}