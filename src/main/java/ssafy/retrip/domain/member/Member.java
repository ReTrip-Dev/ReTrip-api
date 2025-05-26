package ssafy.retrip.domain.member;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.domain.BaseEntity;
import ssafy.retrip.domain.retrip.Retrip;

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

  // Retrip과의 관계 설정 (1:N)

  @Column(unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  private LoginType loginType;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Retrip> retrips = new ArrayList<>();

  @Builder
  private Member(String userId, String password, String kakaoId, String nickname, String email,
      LoginType loginType) {
    this.userId = userId;
    this.password = password;
    this.kakaoId = kakaoId;
    this.email = email;
    this.nickname = nickname;
    this.retrips = new ArrayList<>();
  }

  /**
   * Retrip 추가 메서드 (양방향 관계 처리)
   */
  public void addRetrip(Retrip retrip) {
    if (retrip != null && !this.retrips.contains(retrip)) {
      this.retrips.add(retrip);
      if (retrip.getMember() != this) {
        retrip.setMember(this);
      }
    }
  }

  public boolean isNormalMember() {
    return this.loginType == LoginType.NORMAL;
  }

  public boolean isKakaoMember() {
    return this.loginType == LoginType.KAKAO;
  }

  public void updatePassword(String newPassword) {
    this.password = newPassword;
  }
}