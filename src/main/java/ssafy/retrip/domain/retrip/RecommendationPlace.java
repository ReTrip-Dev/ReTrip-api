package ssafy.retrip.domain.retrip;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "recommendation_places")
public class RecommendationPlace extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String emoji;        // 장소 특성을 나타내는 이모지
    
    @Column(nullable = false)
    private String place;        // 추천 장소명
    
    @Column(nullable = false)
    private String description;  // 장소에 대한 간략한 설명
    
    // Retrip과의 관계는 Retrip 엔터티에서 관리
}
