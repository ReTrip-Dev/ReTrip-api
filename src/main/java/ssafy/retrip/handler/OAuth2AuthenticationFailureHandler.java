package ssafy.retrip.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

  private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  private static final String REDIRECT_URL = "/login/callback";
  private static final String FRONT_SERVER = "http://localhost:5173";

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
    String targetUrl = getTargetUrl(errorMessage);

    redirectStrategy.sendRedirect(request, response, targetUrl);
  }

  private String getTargetUrl(String errorMessage) {
    return UriComponentsBuilder
        .fromUriString(FRONT_SERVER + REDIRECT_URL)
        .queryParam("error", errorMessage)
        .build().toUriString();

  }
}
