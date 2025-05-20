package ssafy.retrip.api.service.oauth.request;

import static lombok.AccessLevel.PROTECTED;

import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.oauth.info.KakaoUserInfo;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class KakaoUserCreateServiceRequest {

  private String sub;

  private String email;

  private String nickname;

  public KakaoUserInfo toKakaoUserInfo() {
    return KakaoUserInfo.builder()
        .sub(sub)
        .email(email)
        .nickname(nickname).build();
  }
}
