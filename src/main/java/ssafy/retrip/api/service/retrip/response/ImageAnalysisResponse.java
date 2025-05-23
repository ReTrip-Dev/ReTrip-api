package ssafy.retrip.api.service.retrip.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisResponse {
    private List<FailedImageInfo> failed_images_info;
    private TravelImageAnalysis travel_image_analysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedImageInfo {
        private String id;
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelImageAnalysis {
        private TravelAnalysis travel_analysis;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelAnalysis {
        private String mbti;
        private String overall_mood;
        private Map<String, String> photo_category_ratio;
        private List<Subject> top5_subjects;
        private String top_visit_place;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Subject {
        private int count;
        private String subject;
    }
}
