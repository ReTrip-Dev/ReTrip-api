package ssafy.retrip.handler;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.RedirectStrategy;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

  @Mock
  private RedirectStrategy redirectStrategy;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  AuthenticationException exception;

  @InjectMocks
  private OAuth2AuthenticationFailureHandler handler;

  @Test
  @DisplayName("로그인 실패 시 에러 메시지를 포함한 URL로 리다이렉트한다.")
  void failureHandlerRedirectsWithErrorMessage() throws Exception {
    // given
    String errorMessage = "인증 실패";
    String encodedError = URLEncoder.encode(errorMessage, UTF_8);
    String expectedUrl = "http://localhost:5173/login/callback?error=" + encodedError;
    given(exception.getMessage()).willReturn(errorMessage);

    // when
    handler.onAuthenticationFailure(request, response, exception);
    // then
    verify(redirectStrategy).sendRedirect(eq(request), eq(response), eq(expectedUrl));
  }
}