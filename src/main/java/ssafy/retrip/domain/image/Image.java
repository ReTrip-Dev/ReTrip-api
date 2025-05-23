package ssafy.retrip.domain.image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.retrip.Retrip;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "images")
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    private String contentType;

    private Long fileSize;

    private LocalDateTime takenDate;

    private String location;

    private Double latitude;

    private Double longitude;
    
    private String dirName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retrip_id")
    private Retrip retrip;

    /**
     * Retrip 연관관계 설정 메서드 (개선된 양방향 관계 설정)
     * @param retrip 연결할 Retrip 객체
     */
    public void setRetrip(Retrip retrip) {
        // 자기 자신과의 비교를 통해 이미 같은 retrip이라면 작업 중단 (무한 루프 방지)
        if (this.retrip == retrip) {
            return;
        }
        
        // 기존 연결이 있으면 제거
        if (this.retrip != null) {
            this.retrip.getImages().remove(this);
        }
        
        this.retrip = retrip;
        
        // 새 연결 추가 (retrip이 null이 아니고, retrip의 이미지 리스트에 현재 이미지가 없는 경우)
        if (retrip != null && !retrip.getImages().contains(this)) {
            retrip.getImages().add(this);
        }
    }
}
