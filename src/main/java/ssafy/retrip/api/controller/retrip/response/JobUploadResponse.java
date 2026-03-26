package ssafy.retrip.api.controller.retrip.response;

import static ssafy.retrip.domain.job.JobStatus.PROCESSING;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ssafy.retrip.domain.job.JobStatus;

@Getter
@AllArgsConstructor
public class JobUploadResponse {

  private String jobId;
  private JobStatus status;
  private String message;

  public static JobUploadResponse of(String jobId) {
    return new JobUploadResponse(jobId, PROCESSING, "이미지 업로드 완료. SSE 또는 폴링으로 결과를 확인하세요.");
  }
}
