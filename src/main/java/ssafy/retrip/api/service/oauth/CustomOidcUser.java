package ssafy.retrip.api.service.oauth;

import java.util.Collection;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import ssafy.retrip.api.service.oauth.info.KakaoUserInfo;

@Getter
public class CustomOidcUser implements OidcUser {

  private final OidcUser oidcUser;
  private final KakaoUserInfo info;

  @Builder
  private CustomOidcUser(OidcUser oidcUser, KakaoUserInfo info) {
    this.oidcUser = oidcUser;
    this.info = info;
  }

  @Override
  public String getName() {
    return oidcUser.getName();
  }

  @Override
  public OidcIdToken getIdToken() {
    return oidcUser.getIdToken();
  }

  @Override
  public OidcUserInfo getUserInfo() {
    return oidcUser.getUserInfo();
  }

  @Override
  public Map<String, Object> getClaims() {
    return oidcUser.getClaims();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return oidcUser.getAttributes();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return oidcUser.getAuthorities();
  }
}
