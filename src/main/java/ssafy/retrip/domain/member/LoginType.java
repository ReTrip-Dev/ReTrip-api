package ssafy.retrip.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginType {

  KAKAO("KAKAO"),
  GOOGLE("GOOGLE"),
  NAVER("NAVER"),
  APPLE("APPLE"),
  NORMAL("NORMAL");

  private final String type;
}