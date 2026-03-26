package ssafy.retrip.api.controller.retrip.response;


import static ssafy.retrip.domain.job.JobStatus.COMPLETED;
import static ssafy.retrip.domain.job.JobStatus.FAILED;
import static ssafy.retrip.domain.job.JobStatus.PROCESSING;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.job.JobStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusResponse {

  private String jobId;
  private JobStatus status;
  private TravelAnalysisResponseDto result;
  private String errorMessage;

  public static JobStatusResponse processing(String jobId) {
    return JobStatusResponse.builder()
        .jobId(jobId)
        .status(PROCESSING)
        .build();
  }

  public static JobStatusResponse completed(String jobId, TravelAnalysisResponseDto result) {
    return JobStatusResponse.builder()
        .jobId(jobId)
        .status(COMPLETED)
        .result(result)
        .build();
  }

  public static JobStatusResponse failed(String jobId, String errorMessage) {
    return JobStatusResponse.builder()
        .jobId(jobId)
        .status(FAILED)
        .errorMessage(errorMessage)
        .build();
  }
}
