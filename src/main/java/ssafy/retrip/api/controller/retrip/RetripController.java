package ssafy.retrip.api.controller.retrip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ssafy.retrip.api.controller.retrip.request.LambdaCallbackRequest;
import ssafy.retrip.api.controller.retrip.request.LambdaFailureRequest;
import ssafy.retrip.api.controller.retrip.response.JobStatusResponse;
import ssafy.retrip.api.controller.retrip.response.JobUploadResponse;
import ssafy.retrip.api.service.retrip.RetripService;
import ssafy.retrip.api.service.sse.SseService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RetripController {

  private final SseService sseService;
  private final RetripService retripService;

  @PostMapping("/images/uploads")
  public Mono<ResponseEntity<JobUploadResponse>> uploadImages(@RequestPart(value = "images") Flux<FilePart> images) {

    return retripService.uploadImagesToS3(images)
        .map(jobId -> {
          log.info("이미지 업로드 완료, jobId 반환: {}", jobId);
          return ResponseEntity.ok(JobUploadResponse.of(jobId));
        })
        .onErrorResume(ex -> {
          log.error("이미지 업로드 중 오류 발생", ex);
          return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));
        });
  }

  @GetMapping(value = "/retrips/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> streamResult(@PathVariable String jobId) {
    log.info("SSE 연결 요청: jobId={}", jobId);
    return sseService.connect(jobId);
  }

  @PostMapping("/internal/retrips/{jobId}/complete")
  public Mono<ResponseEntity<Void>> handleLambdaCallback(
      @PathVariable String jobId,
      @RequestBody LambdaCallbackRequest request
  ) {

    log.info("Lambda 콜백 수신: jobId={}", jobId);

    return retripService.handleAnalysisResult(jobId, request)
        .then(Mono.just(ResponseEntity.ok().<Void>build()))
        .onErrorResume(ex -> {
          log.error("Lambda 콜백 처리 실패: jobId={}", jobId, ex);
          return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
  }

  @PostMapping("/internal/retrips/{jobId}/fail")
  public Mono<ResponseEntity<Void>> handleLambdaFailure(
      @PathVariable String jobId,
      @RequestBody LambdaFailureRequest request
  ) {

    log.error("Lambda 실패 콜백 수신: jobId={}, error={}", jobId, request.getErrorMessage());

    return retripService.handleFailure(jobId, request.getErrorMessage())
        .then(Mono.just(ResponseEntity.ok().<Void>build()))
        .onErrorResume(ex -> {
          log.error("Lambda 실패 콜백 처리 중 오류: jobId={}", jobId, ex);
          return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        });
  }

  @GetMapping("/retrips/{jobId}/status")
  public Mono<ResponseEntity<JobStatusResponse>> getJobStatus(@PathVariable String jobId) {
    return retripService.getJobStatus(jobId)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
