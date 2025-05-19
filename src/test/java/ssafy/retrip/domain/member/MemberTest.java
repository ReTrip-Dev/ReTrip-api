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
    assertThat(savedMember).extracting("id", "kakaoId", "nickname", "profileImageUrl")
        .containsExactly(1L, "1234567890", "nickname", "http://localhost:8080");
  }

  private Member createMember() {

    return Member.builder()
        .kakaoId("1234567890")
        .nickname("nickname")
        .profileImageUrl("http://localhost:8080").build();
  }
}