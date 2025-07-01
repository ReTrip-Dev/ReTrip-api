package ssafy.retrip.api.service.oauth.info;

import static lombok.AccessLevel.PROTECTED;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class KakaoUserInfo {

  private String sub;

  private String email;

  private String nickname;

  @Builder
  public KakaoUserInfo(String sub, String email, String nickname) {
    this.sub = sub;
    this.email = email;
    this.nickname = nickname;
  }
}
