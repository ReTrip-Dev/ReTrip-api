package ssafy.retrip.api.service.s3;

import static ssafy.retrip.utils.ConstantUtil.BUCKET_PREFIX;
import static ssafy.retrip.utils.ConstantUtil.BUCKET_SUFFIX;
import static ssafy.retrip.utils.ConstantUtil.DEFAULT_CONTENT_TYPE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  @Value("${aws.s3.bucket}")
  private String bucket;

  private final S3Client s3Client;

  public Mono<Void> uploadFile(String key, byte[] data, String contentType) {
    return Mono.fromCallable(() -> {
          s3Client.putObject(
              PutObjectRequest.builder()
                  .bucket(bucket)
                  .key(key)
                  .contentType(contentType)
                  .build(),
              RequestBody.fromBytes(data)
          );
          log.debug("S3 업로드 완료: {}", key);
          return true;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  public Mono<Void> uploadCompletionMarker(String jobId) {
    String key = BUCKET_PREFIX + jobId + BUCKET_SUFFIX;
    return uploadFile(key, new byte[0], DEFAULT_CONTENT_TYPE)
        .doOnSuccess(v -> log.info("S3 완료 마커 업로드: jobId={}", jobId));
  }
}