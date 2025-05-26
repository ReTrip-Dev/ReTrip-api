package ssafy.retrip.domain.retrip;

import jakarta.persistence.*;
import lombok.*;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.image.Image;
import ssafy.retrip.domain.member.Member;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "retrips")
public class Retrip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member와의 관계 설정 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 여행 총 이동거리 (킬로미터)
    private Double totalDistance;

    // 주요 촬영 시간대
    @Enumerated(EnumType.STRING)
    private TimeSlot mainTimeSlot;

    // 가장 많은 사진 촬영 장소
    private String mainLocation;

    // 가장 많은 사진 촬영 장소 좌표
    private Double mainLocationLat;
    private Double mainLocationLng;

    // 여행 시작/종료 시간
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // 총 이미지 개수
    private Integer imageCount;

    // 여행 이름
    private String name;
    
    // ChatGPT 분석 결과 필드 - 새 JSON 구조에 맞게 수정
    // User 정보
    private String countryCode;   // 방문한 국가의 ISO 코드
    private String mbti;          // 사용자의 여행 스타일 MBTI
    
    // TripSummary 정보
    private String summaryLine;   // 여행 한 줄 요약(20자 이내)
    
    @ElementCollection
    @CollectionTable(name = "retrip_keywords", joinColumns = @JoinColumn(name = "retrip_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();  // 여행 키워드 3개
    
    private String hashtag;       // 단일 단어 해시태그
    
    // PhotoStats 정보
    @ElementCollection
    @CollectionTable(name = "retrip_favorite_subjects", joinColumns = @JoinColumn(name = "retrip_id"))
    @Column(name = "subject")
    private List<String> favoriteSubjects = new ArrayList<>();  // 피사체 이모지 3개
    
    private String favoritePhotoSpot;  // 가장 많이 촬영된 장소명
    
    // Recommendations 정보 (별도 테이블로 저장)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "retrip_id")
    private List<RecommendationPlace> recommendations = new ArrayList<>();

    // 여행 설명 (ChatGPT API로 생성 가능)
    private String description;

    // Image와의 관계 설정 (양방향)
    @OneToMany(mappedBy = "retrip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> images = new ArrayList<>();
    
    /**
     * 이미지 추가 메서드 (1:N 관계 설정을 위한 편의 메서드)
     * @param image 추가할 이미지
     */
    public void addImage(Image image) {
        // 이미 존재하는지 확인 (중복 추가 방지)
        if (this.images.contains(image)) {
            return;
        }
        
        this.images.add(image);
        if (image.getRetrip() != this) {
            image.setRetrip(this);
        }
    }
    
    /**
     * 이미지 다수 추가 메서드
     * @param images 추가할 이미지 목록
     */
    public void addImages(List<Image> images) {
        if (images != null) {
            images.forEach(this::addImage);
        }
    }
    
    /**
     * 이미지 제거 메서드
     * @param image 제거할 이미지
     */
    public void removeImage(Image image) {
        if (!this.images.contains(image)) {
            return;
        }
        
        this.images.remove(image);
        if (image.getRetrip() == this) {
            image.setRetrip(null);
        }
    }

    /**
     * Member 설정 메서드 (양방향 관계 처리)
     * @param member 설정할 Member 객체
     */
    public void setMember(Member member) {
        this.member = member;
        if (member != null && !member.getRetrips().contains(this)) {
            member.getRetrips().add(this);
        }
    }
    
    /**
     * 추천 장소 추가 메서드
     * @param recommendation 추가할 추천 장소
     */
    public void addRecommendation(RecommendationPlace recommendation) {
        this.recommendations.add(recommendation);
    }
}
