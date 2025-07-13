package ssafy.retrip.domain.place;

import static lombok.AccessLevel.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.retrip.Retrip;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "retrip_id")
  @JsonBackReference
  private Retrip retrip;

  @Builder
  private RecommendationPlace(String emoji, String place, String description) {
    this.emoji = emoji;
    this.place = place;
    this.description = description;
  }

  public void updateRetrip(Retrip retrip) {
    this.retrip = retrip;
  }
}