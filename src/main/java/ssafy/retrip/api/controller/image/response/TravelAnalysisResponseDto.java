package ssafy.retrip.api.controller.image.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.vision.request.AnalysisResponse.TravelImageAnalysis;
import ssafy.retrip.domain.retrip.Retrip;
import ssafy.retrip.domain.retrip.TimeSlot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelAnalysisResponseDto {
    private UserDto user;
    private TripSummaryDto tripSummary;
    private PhotoStatsDto photoStats;
    private TravelStatsDto travelStats;
    private List<RecommendationDto> recommendations;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDto {
        private String username;
        private String countryCode;
        private String mbti;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TripSummaryDto {
        private String summaryLine;
        private List<String> keywords;
        private String tripDates;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PhotoStatsDto {
        private List<String> favoriteSubjects;
        private String favoritePhotoSpot;
        private String favoritePhotoTime;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TravelStatsDto {
        private String travelDistance;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationDto {
        private String emoji;
        private String place;
        private String description;
    }

    // TravelImageAnalysis와 Retrip 정보를 바탕으로 응답 DTO 생성
    public static TravelAnalysisResponseDto from(TravelImageAnalysis analysis, Retrip retrip, String username) {
        return TravelAnalysisResponseDto.builder()
                .user(UserDto.builder()
                        .username(username)
                        .countryCode(analysis.getUser().getCountryCode())
                        .mbti(analysis.getUser().getMbti())
                        .build())
                .tripSummary(TripSummaryDto.builder()
                        .summaryLine(analysis.getTripSummary().getSummaryLine())
                        .keywords(analysis.getTripSummary().getKeywords())
                        .tripDates(formatDateRange(retrip.getStartDate(), retrip.getEndDate()))
                        .build())
                .photoStats(PhotoStatsDto.builder()
                        .favoriteSubjects(analysis.getPhotoStats().getFavoriteSubjects())
                        .favoritePhotoSpot(analysis.getPhotoStats().getFavoritePhotoSpot())
                        .favoritePhotoTime(getTimeEmoji(retrip.getMainTimeSlot()))
                        .build())
                .travelStats(TravelStatsDto.builder()
                        .travelDistance("총 " + Math.round(retrip.getTotalDistance()) + "km")
                        .build())
                .recommendations(retrip.getRecommendations().stream()
                        .map(rec -> RecommendationDto.builder()
                                .emoji(rec.getEmoji())
                                .place(rec.getPlace())
                                .description(rec.getDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private static String formatDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return start.format(formatter) + " - " + end.format(formatter);
    }

    private static String getTimeEmoji(TimeSlot timeSlot) {
        if (timeSlot == null) return "⏱️ 알 수 없음";

        switch (timeSlot) {
            case MORNING: return "🌅 아침";
            case AFTERNOON: return "🌞 낮";
            case DAWN: return "🌆 새벽";
            case NIGHT: return "🌃 밤";
            default: return "⏱️ 기타";
        }
    }
}