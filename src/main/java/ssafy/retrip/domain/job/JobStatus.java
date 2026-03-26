package ssafy.retrip.domain.job;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum JobStatus {
  PROCESSING,
  COMPLETED,
  FAILED
}