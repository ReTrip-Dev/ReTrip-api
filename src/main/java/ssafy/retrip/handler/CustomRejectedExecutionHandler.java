package ssafy.retrip.handler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    log.warn("스레드 풀 포화: 작업 거부됨. ActiveThreads={}, QueueSize={}",
        executor.getActiveCount(), executor.getQueue().size());

    if (!executor.isShutdown()) {
      try {
        log.info("호출 스레드에서 작업 실행 재시도");
        r.run();
      } catch (Exception e) {
        log.error("거부된 작업 재실행 중 오류 발생", e);
      }
    }
  }
}
