package ssafy.retrip.domain.retrip;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.place.RecommendationPlace;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "retrips")
public class Retrip extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // --- GPT 분석 결과 ---
  private String countryCode;
  private String mbti;
  private String egenTetoType;
  private String egenTetoSubtype;
  private String egenTetoHashtag;
  private String summaryLine;
  private String hashtag;
  private String favoriteSubjects;
  private String favoritePhotoSpot;
  @Column(columnDefinition = "TEXT")
  private String keywords;

  // --- 이미지 메타데이터 기반 통계 ---
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Integer imageCount;
  private Double totalDistance;
  @Enumerated(EnumType.STRING)
  private TimeSlot mainTimeSlot;
  private Double mainLocationLat;
  private Double mainLocationLng;

  @OneToMany(mappedBy = "retrip", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private List<RecommendationPlace> recommendations = new ArrayList<>();

  public void addRecommendation(RecommendationPlace recommendation) {
    recommendations.add(recommendation);
    recommendation.setRetrip(this);
  }

  public void updateRetripDetailsData(
      LocalDateTime startDate,
      LocalDateTime endDate,
      Integer imageCount,
      Double totalDistance,
      TimeSlot mainTimeSlot,
      Double mainLocationLat,
      Double mainLocationLng
  ) {
    this.startDate = startDate;
    this.endDate = endDate;
    this.imageCount = imageCount;
    this.totalDistance = totalDistance;
    this.mainTimeSlot = mainTimeSlot;
    this.mainLocationLat = mainLocationLat;
    this.mainLocationLng = mainLocationLng;
  }
}