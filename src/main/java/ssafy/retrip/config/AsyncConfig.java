package ssafy.retrip.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ssafy.retrip.handler.CustomRejectedExecutionHandler;

@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean(name = "openAiExecutor")
  public Executor openAiExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("openAiExecutor-");
    executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());
    executor.initialize();
    return executor;
  }
}
