package ssafy.retrip.api.service.sse;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import ssafy.retrip.api.service.cache.RetripResultCacheService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

  private final ConcurrentHashMap<String, Sinks.One<String>> sinkMap = new ConcurrentHashMap<>();
  private final RetripResultCacheService cacheService;

  public Flux<ServerSentEvent<String>> connect(String jobId) {
    return cacheService.getCachedResult(jobId)
        .flatMapMany(cached -> {
          log.info("SSE 재연결 - Redis 캐시에서 결과 전송: jobId={}", jobId);
          return Flux.just(
              ServerSentEvent.<String>builder()
                  .event("result")
                  .data(cached)
                  .build()
          );
        })
        .switchIfEmpty(createSseStream(jobId));
  }

  private Flux<ServerSentEvent<String>> createSseStream(String jobId) {
    Sinks.One<String> sink = Sinks.one();
    sinkMap.put(jobId, sink);

    Flux<ServerSentEvent<String>> resultStream = sink.asMono()
        .map(data -> ServerSentEvent.<String>builder()
            .event("result")
            .data(data)
            .build())
        .flux();

    Flux<ServerSentEvent<String>> heartbeat = Flux.interval(Duration.ofSeconds(15))
        .map(i -> ServerSentEvent.<String>builder()
            .comment("heartbeat")
            .build());

    return resultStream.mergeWith(heartbeat)
        .timeout(Duration.ofMinutes(5))
        .doOnSubscribe(sub -> log.info("SSE 연결 수립: jobId={}", jobId))
        .doFinally(signal -> {
          sinkMap.remove(jobId);
          log.info("SSE 연결 종료: jobId={}, signal={}", jobId, signal);
        });
  }

  public void pushResult(String jobId, String resultJson) {
    Sinks.One<String> sink = sinkMap.get(jobId);
    if (sink != null) {
      Sinks.EmitResult emitResult = sink.tryEmitValue(resultJson);
      if (emitResult.isSuccess()) {
        log.info("SSE 푸시 성공: jobId={}", jobId);
      } else {
        log.warn("SSE 푸시 실패: jobId={}, result={}", jobId, emitResult);
      }
    } else {
      log.info("SSE 연결 없음 (클라이언트가 폴링 사용 중일 수 있음): jobId={}", jobId);
    }
  }

  public void pushError(String jobId, String errorMessage) {
    Sinks.One<String> sink = sinkMap.get(jobId);
    if (sink != null) {
      sink.tryEmitError(new RuntimeException(errorMessage));
      log.warn("SSE 에러 푸시: jobId={}, error={}", jobId, errorMessage);
    } else {
      log.info("SSE 연결 없음 - 에러 푸시 생략: jobId={}", jobId);
    }
  }
}
