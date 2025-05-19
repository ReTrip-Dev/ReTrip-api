package ssafy.retrip.domain.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import ssafy.retrip.domain.BaseEntity;

@Entity
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
