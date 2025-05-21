package ssafy.retrip.handler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.RedirectStrategy;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RedirectStrategy redirectStrategy;

  @Mock
  private OidcUser oidcUser;

  @Mock
  private HttpSession session;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private OAuth2AuthenticationSuccessHandler handler;

  private final String REDIRECT_URL = "/";
  private final String kakaoId = "1234567890";
  private final String email = "test@email.com";
  private final String nickname = "tester";

  @Test
  @DisplayName("회원이 존재하면 세션에 저장하고 리다이렉트한다.")
  void existMemberSuccessHandlerTest() throws Exception {

    // given
    Member member = createMember();

    given(oidcUser.getSubject()).willReturn(kakaoId);
    given(oidcUser.getEmail()).willReturn(email);
    given(oidcUser.getAttribute("nickname")).willReturn(nickname);
    given(memberRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(member));
    given(authentication.getPrincipal()).willReturn(oidcUser);
    given(request.getSession()).willReturn(session);

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(memberRepository).findByKakaoId(kakaoId);
    verify(session).setAttribute("member", kakaoId);
    verify(redirectStrategy).sendRedirect(request, response, REDIRECT_URL);
    verifyNoMoreInteractions(redirectStrategy);
  }

  @Test
  @DisplayName("회원이 존재하지 않으면 회원을 생성하고 세션에 저장한 후 리다이렉트한다.")
  void notExistMemberSuccessHandler() throws Exception {

    // given
    Member newMember = createMember();

    given(oidcUser.getSubject()).willReturn(kakaoId);
    given(oidcUser.getEmail()).willReturn(email);
    given(oidcUser.getAttribute("nickname")).willReturn(nickname);
    given(memberRepository.findByKakaoId(kakaoId)).willReturn(Optional.empty());
    given(memberRepository.save(any(Member.class))).willReturn(newMember);
    given(authentication.getPrincipal()).willReturn(oidcUser);
    given(request.getSession()).willReturn(session);

    // when
    handler.onAuthenticationSuccess(request, response, authentication);

    // then
    verify(memberRepository).save(any(Member.class));
    verify(session).setAttribute("member", kakaoId);
    verify(redirectStrategy).sendRedirect(request, response, REDIRECT_URL);
  }

  private Member createMember() {
    return Member.builder()
        .kakaoId(this.kakaoId)
        .email(this.email)
        .nickname(this.nickname).build();
  }
}