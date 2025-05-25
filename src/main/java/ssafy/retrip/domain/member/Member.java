package ssafy.retrip.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;

@Getter
@Entity
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String userId;

  private String password;

  @Column(unique = true)
  private String kakaoId;

  private String nickname;

  @Column(unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  private LoginType loginType;

  @Builder
  private Member(String userId, String password, String kakaoId, String nickname, String email,
      LoginType loginType) {
    this.userId = userId;
    this.password = password;
    this.kakaoId = kakaoId;
    this.nickname = nickname;
    this.email = email;
    this.loginType = loginType;
  }

  public boolean isNormalMember() {
    return this.loginType == LoginType.NORMAL;
  }

  public boolean isKakaoMember() {
    return this.loginType == LoginType.KAKAO;
  }
}