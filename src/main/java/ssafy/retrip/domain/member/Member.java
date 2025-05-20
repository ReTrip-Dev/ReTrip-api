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

  private String email;

  private String nickname;

  @Builder
  private Member(String kakaoId, String email, String nickname) {
    this.kakaoId = kakaoId;
    this.email = email;
    this.nickname = nickname;
  }
}
