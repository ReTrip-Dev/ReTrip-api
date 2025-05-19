package ssafy.retrip.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  private String kakaoId;

  private String nickname;

  private String profileImageUrl;

  @Builder
  private Member(String kakaoId, String nickname, String profileImageUrl) {
    this.kakaoId = kakaoId;
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
  }
}
