package ssafy.retrip.api.service.retrip;

import static ssafy.retrip.domain.job.JobStatus.COMPLETED;
import static ssafy.retrip.domain.job.JobStatus.FAILED;
import static ssafy.retrip.domain.job.JobStatus.PROCESSING;
import static ssafy.retrip.utils.ConstantUtil.BUCKET_PREFIX;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ssafy.retrip.api.controller.retrip.request.LambdaCallbackRequest;
import ssafy.retrip.api.controller.retrip.response.JobStatusResponse;
import ssafy.retrip.api.controller.retrip.response.TravelAnalysisResponseDto;
import ssafy.retrip.api.service.cache.RetripResultCacheService;
import ssafy.retrip.api.service.openai.response.AnalysisResponse;
import ssafy.retrip.api.service.retrip.info.ImageMetaData;
import ssafy.retrip.api.service.s3.S3Service;
import ssafy.retrip.api.service.sse.SseService;
import ssafy.retrip.domain.job.RetripJob;
import ssafy.retrip.domain.job.RetripJobRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripService {

  private final S3Service s3Service;
  private final SseService sseService;
  private final RetripResultCacheService cacheService;
  private final RetripPersistenceService persistenceService;
  private final RetripJobRepository retripJobRepository;
  private final TransactionTemplate transactionTemplate;
  private final ObjectMapper objectMapper;

  @Value("${retrip.image.max-count:20}")
  private int maxImageCount;

  @Value("${retrip.image.max-size-mb:10}")
  private int maxSizeMb;

  public Mono<String> uploadImagesToS3(Flux<FilePart> images) {
    String jobId = UUID.randomUUID().toString();

    return images
        .take(maxImageCount)
        .flatMap(filePart -> uploadSingleFile(jobId, filePart))
        .collectList()
        .flatMap(uploadedKeys -> {
          log.info("S3 업로드 완료: jobId={}, 파일 수={}", jobId, uploadedKeys.size());

          if (uploadedKeys.isEmpty()) {
            return Mono.error(new IllegalArgumentException("업로드된 이미지가 없습니다."));
          }

          return s3Service
              .uploadCompletionMarker(jobId)
              .then(createJob(jobId));
        })
        .thenReturn(jobId);
  }

  public Mono<Void> handleAnalysisResult(String jobId, LambdaCallbackRequest request) {
    AnalysisResponse analysisResponse = request.getAnalysisResponse();
    List<ImageMetaData> metadata = request.toImageMetaDataList();

    return persistenceService.saveRetripReactive(analysisResponse, metadata)
        .flatMap(result -> {

          updateJobStatus(jobId, result.getRetripId());

          String resultJson;
          try {
            resultJson = objectMapper.writeValueAsString(result);
          } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("결과 직렬화 실패", e));
          }

          return cacheService.cacheResult(jobId, resultJson)
              .doOnSuccess(v -> {
                sseService.pushResult(jobId, resultJson);
                log.info("분석 결과 처리 완료: jobId={}, retripId={}", jobId, result.getRetripId());
              });
        });
  }

  public Mono<JobStatusResponse> getJobStatus(String jobId) {
    return cacheService.getCachedResult(jobId)
        .flatMap(cachedJson -> {
          try {
            TravelAnalysisResponseDto result = objectMapper.readValue(cachedJson, TravelAnalysisResponseDto.class);
            return Mono.just(JobStatusResponse.completed(jobId, result));
          } catch (JsonProcessingException e) {
            return Mono.just(JobStatusResponse.completed(jobId, null));
          }
        })
        .switchIfEmpty(Mono.fromCallable(() ->
                retripJobRepository.findByJobId(jobId)
                    .map(job -> {
                      if (job.getStatus() == COMPLETED) {
                        return JobStatusResponse.completed(jobId, null);
                      } else if (job.getStatus() == FAILED) {
                        return JobStatusResponse.failed(jobId, job.getErrorMessage());
                      }
                      return JobStatusResponse.processing(jobId);
                    })
                    .orElse(null))
            .subscribeOn(Schedulers.boundedElastic()));
  }

  private Mono<String> uploadSingleFile(String jobId, FilePart filePart) {
    String filename = filePart.filename();
    String contentType = resolveContentType(filePart);
    String key = BUCKET_PREFIX + jobId + "/" + filename;

    return DataBufferUtils.join(filePart.content())
        .flatMap(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          DataBufferUtils.release(dataBuffer);

          if (bytes.length > maxSizeMb * 1024L * 1024L) {
            return Mono.error(new IllegalArgumentException(
                String.format("이미지 크기가 %dMB를 초과합니다: %s", maxSizeMb, filename)));
          }

          return s3Service
              .uploadFile(key, bytes, contentType)
              .thenReturn(key);
        });
  }

  private String resolveContentType(FilePart filePart) {
    MediaType mediaType = filePart.headers().getContentType();
    if (mediaType != null && mediaType.getType().equals("image")) {
      return mediaType.toString();
    }

    String filename = filePart.filename().toLowerCase();
    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
    if (filename.endsWith(".png"))  return "image/png";
    if (filename.endsWith(".gif"))  return "image/gif";
    if (filename.endsWith(".webp")) return "image/webp";
    if (filename.endsWith(".heic")) return "image/heic";
    if (filename.endsWith(".heif")) return "image/heif";
    return "application/octet-stream";
  }

  private Mono<Void> createJob(String jobId) {
    return Mono.fromCallable(() ->
            transactionTemplate.execute(status -> {
              RetripJob job = RetripJob.builder()
                  .jobId(jobId)
                  .status(PROCESSING)
                  .build();
              retripJobRepository.save(job);
              log.info("RetripJob 생성: jobId={}", jobId);
              return job;
            }))
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  public Mono<Void> handleFailure(String jobId, String errorMessage) {
    return Mono.fromCallable(() ->
            transactionTemplate.execute(status -> {
              retripJobRepository.findByJobId(jobId).ifPresent(job -> {
                job.fail(errorMessage);
                retripJobRepository.save(job);
                log.error("RetripJob 실패 처리: jobId={}, error={}", jobId, errorMessage);
              });
              return jobId;
            }))
        .subscribeOn(Schedulers.boundedElastic())
        .doOnSuccess(v -> sseService.pushError(jobId, errorMessage))
        .then();
  }

  private void updateJobStatus(String jobId, Long retripId) {
    transactionTemplate.executeWithoutResult(status ->
        retripJobRepository.findByJobId(jobId).ifPresent(job -> {
          job.complete(retripId);
          retripJobRepository.save(job);
          log.info("RetripJob 완료: jobId={}, retripId={}", jobId, retripId);
        })
    );
  }
}
