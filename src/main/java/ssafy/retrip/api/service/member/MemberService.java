package ssafy.retrip.api.service.member;

import static ssafy.retrip.domain.member.LoginType.NORMAL;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.api.service.email.EmailService;
import ssafy.retrip.api.service.member.request.MemberSignInServiceRequest;
import ssafy.retrip.api.service.member.request.MemberSignUpServiceRequest;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;
import ssafy.retrip.global.exception.NicknameAlreadyExistsException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final EmailService emailService;
  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Transactional
  public void signup(MemberSignUpServiceRequest request) {

    String encodedPassword = passwordEncoder.encode(request.getPassword());
    Member member = Member.builder()
        .userId(request.getUserId())
        .password(encodedPassword)
        .email(request.getEmail())
        .loginType(NORMAL).build();

    memberRepository.save(member);
  }

  public void validateNickname(String nickname) {
    if (memberRepository.existsByUserId(nickname)) {
      throw new NicknameAlreadyExistsException("이미 사용 중인 닉네임입니다.");
    }
  }

  public void signIn(MemberSignInServiceRequest request) {

    Member member = memberRepository.findByUserId(request.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }
  }

  public void sendVerificationCode(String email) {
    emailService.findForgotUserId(email);
  }

  public void findForgotUserPw() {
  }

  public String getForgotUserId(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    return member.getUserId();
  }
}
