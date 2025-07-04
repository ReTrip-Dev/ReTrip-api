package ssafy.retrip.domain.retrip;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.member.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "retrips")
public class Retrip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- GPT 분석 결과 ---
    private String countryCode;
    private String mbti;
    private String egenTetoType; // EGEN 또는 TETO
    private String egenTetoSubtype; // 세부 유형 (예: "귀족의 피가 흐르는 에겐녀")
    private String egenTetoHashtag; // #에겐 또는 #테토
    private String summaryLine;
    private String hashtag;
    private String favoriteSubjects; // 쉼표로 구분된 문자열
    private String favoritePhotoSpot; // GPT가 추정한 주요 촬영 장소명
    @Column(columnDefinition = "TEXT")
    private String keywords; // 쉼표로 구분된 문자열

    // --- 이미지 메타데이터 기반 통계 ---
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer imageCount;
    private Double totalDistance;
    @Enumerated(EnumType.STRING)
    private TimeSlot mainTimeSlot;
    private Double mainLocationLat; // 주로 촬영된 위치의 위도
    private Double mainLocationLng; // 주로 촬영된 위치의 경도

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 추천 장소와의 1:N 연관관계
    @OneToMany(mappedBy = "retrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<RecommendationPlace> recommendations = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addRecommendation(RecommendationPlace recommendation) {
        recommendations.add(recommendation);
        recommendation.setRetrip(this);
    }
}