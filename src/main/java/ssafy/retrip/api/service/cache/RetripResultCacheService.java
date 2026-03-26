package ssafy.retrip.api.service.cache;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetripResultCacheService {

  private static final String KEY_PREFIX = "retrip:result:";
  private static final Duration CACHE_TTL = Duration.ofMinutes(10);

  private final RedisTemplate<String, String> redisTemplate;

  public Mono<Void> cacheResult(String jobId, String resultJson) {
    return Mono.fromRunnable(() -> {
          String key = KEY_PREFIX + jobId;
          redisTemplate.opsForValue().set(key, resultJson, CACHE_TTL);
          log.info("Redis 결과 캐싱 완료: jobId={}, TTL={}분", jobId, CACHE_TTL.toMinutes());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then();
  }

  public Mono<String> getCachedResult(String jobId) {
    return Mono.fromCallable(() -> {
          String key = KEY_PREFIX + jobId;
          return redisTemplate.opsForValue().get(key);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(result -> result != null ? Mono.just(result) : Mono.empty());
  }
}
