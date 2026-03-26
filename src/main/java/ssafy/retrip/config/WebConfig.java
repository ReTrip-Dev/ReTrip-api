package ssafy.retrip.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

  private String FRONTEND_LOCAL_SERVER = "http://127.0.0.1:5173";
  private String BACKEND_LOCAL_SERVER = "http://127.0.0.1:8080";

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(
            "https://retrip.vercel.app",
            "https://retrip.kr",
            "https://www.retrip.kr",
            "https://api.retrip.kr",
            FRONTEND_LOCAL_SERVER,
            BACKEND_LOCAL_SERVER
        )
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Authorization")
        .allowCredentials(true);
  }
}