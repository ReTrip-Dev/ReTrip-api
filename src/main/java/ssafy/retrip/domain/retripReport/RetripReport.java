package ssafy.retrip.domain.retripReport;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "retrip_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetripReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String memberId;

  private String imageUrl;


  @Builder
  private RetripReport(String memberId, String imageUrl) {
    this.memberId = memberId;
    this.imageUrl = imageUrl;
  }
}