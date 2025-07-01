package ssafy.retrip.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ssafy.retrip.domain.member.LoginType;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final MemberRepository memberRepository;
  private final RedirectStrategy redirectStrategy;

  private static final String REDIRECT_URL = "http://localhost:5173/photo";
  private static final String SESSION_MEMBER_KEY = "member";

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

    Member member = findMember(oidcUser);

    HttpSession session = request.getSession();
    session.setAttribute(SESSION_MEMBER_KEY, member.getKakaoId());

    redirectStrategy.sendRedirect(request, response, REDIRECT_URL);
  }

  private Member findMember(OidcUser oidcUser) {

    String kakaoId = oidcUser.getSubject();
    String email = oidcUser.getEmail();
    String nickname = oidcUser.getAttribute("nickname");

    return memberRepository.findByKakaoId(kakaoId).orElseGet(
        () -> {
          Member m = Member.builder()
              .kakaoId(kakaoId)
              .email(email)
              .nickname(nickname)
              .loginType(LoginType.KAKAO).build();

          return memberRepository.save(m);
        });
  }
}
