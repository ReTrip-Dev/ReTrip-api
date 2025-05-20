package ssafy.retrip.api.service.oauth;


import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ssafy.retrip.api.service.oauth.info.KakaoUserInfo;
import ssafy.retrip.api.service.oauth.request.KakaoUserCreateServiceRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

  private final RestClient restClient;

  private final String USER_INFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";
  private final String HEADER_NAME = "Authorization";
  private final String HEADER_VALUE = "Bearer ";

  @Override
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

    OidcUser oidcUser = super.loadUser(userRequest);

    KakaoUserInfo info = getKakaoUserInfo(userRequest);

    return CustomOidcUser.builder()
        .oidcUser(oidcUser)
        .info(info).build();
  }

  private KakaoUserInfo getKakaoUserInfo(OidcUserRequest userRequest) {

    String accessToken = userRequest.getAccessToken().getTokenValue();

    KakaoUserCreateServiceRequest request = restClient.get()
        .uri(USER_INFO_URL)
        .header(HEADER_NAME, HEADER_VALUE + accessToken)
        .retrieve()
        .body(KakaoUserCreateServiceRequest.class);

    return Optional.ofNullable(request)
        .map(KakaoUserCreateServiceRequest::toKakaoUserInfo)
        .orElseThrow(IllegalArgumentException::new);
  }
}
