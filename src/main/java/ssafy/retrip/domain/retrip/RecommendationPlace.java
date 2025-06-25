package ssafy.retrip.domain.retrip;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import ssafy.retrip.domain.BaseEntity;

@Entity
@Getter
@Setter // 서비스 로직에서 연관관계 설정을 위해 추가
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recommendation_places")
public class RecommendationPlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String emoji;

    @Column(nullable = false)
    private String place;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Retrip과의 N:1 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retrip_id")
    @JsonBackReference // 순환 참조 방지
    private Retrip retrip;
}