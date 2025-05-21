package ssafy.retrip.domain.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


@DataJpaTest
@ActiveProfiles("test")
class MemberTest {

  @Autowired
  MemberRepository memberRepository;

  @Test
  @DisplayName("회원 정보를 정상적으로 저장할 수 있다.")
  void saveMemberTest() throws Exception {
    // given
    Member member = createMember();

    // when
    Member savedMember = memberRepository.save(member);

    // then
    assertThat(savedMember.getId()).isNotNull();
    assertThat(savedMember).extracting("kakaoId", "nickname", "email")
        .containsExactly("1234567890", "nickname", "1234@naver.com");
  }

  private Member createMember() {

    return Member.builder()
        .kakaoId("1234567890")
        .nickname("nickname")
        .email("1234@naver.com").build();
  }
}