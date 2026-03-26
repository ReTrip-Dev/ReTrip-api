package ssafy.retrip.domain.job;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "retrip_jobs")
public class RetripJob extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 36)
  private String jobId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private JobStatus status = JobStatus.PROCESSING;

  private Long retripId;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  public void complete(Long retripId) {
    this.status = JobStatus.COMPLETED;
    this.retripId = retripId;
  }

  public void fail(String errorMessage) {
    this.status = JobStatus.FAILED;
    this.errorMessage = errorMessage;
  }
}
