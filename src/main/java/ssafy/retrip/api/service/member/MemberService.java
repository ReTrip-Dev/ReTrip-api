package ssafy.retrip.api.service.member;

import static ssafy.retrip.domain.member.LoginType.NORMAL;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.retrip.api.service.email.EmailService;
import ssafy.retrip.api.service.email.request.EmailVerificationServiceRequest;
import ssafy.retrip.api.service.member.request.MemberSignInServiceRequest;
import ssafy.retrip.api.service.member.request.MemberSignUpServiceRequest;
import ssafy.retrip.api.service.member.request.PasswordFindServiceRequest;
import ssafy.retrip.api.service.member.request.PasswordResetServiceRequest;
import ssafy.retrip.api.service.retrip.response.ImageUrlResponse;
import ssafy.retrip.domain.member.Member;
import ssafy.retrip.domain.member.MemberRepository;
import ssafy.retrip.domain.retripReport.RetripReport;
import ssafy.retrip.domain.retripReport.RetripReportRepository;
import ssafy.retrip.global.exception.NicknameAlreadyExistsException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final EmailService emailService;
  private final MemberRepository memberRepository;
  private final BCryptPasswordEncoder passwordEncoder;
  private final RetripReportRepository retripReportRepository;

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

  public String getForgotUserId(String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

    return member.getUserId();
  }

  public void verifyPasswordResetCredentials(PasswordFindServiceRequest request) {

    emailService.verifyEmailCode(EmailVerificationServiceRequest.builder()
        .email(request.getEmail())
        .code(request.getCode()).build());

    Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
        () -> new IllegalArgumentException("회원 정보가 일치하지 않습니다.")
    );

    if (!StringUtils.equals(member.getUserId(), request.getUserId())) {
      throw new IllegalArgumentException("회원 정보가 일치하지 않습니다.");
    }
  }

  @Transactional
  public void resetPassword(PasswordResetServiceRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
        () -> new IllegalArgumentException("존재하지 않는 회원입니다.")
    );

    String encodedPassword = passwordEncoder.encode(request.getNewPassword());
    member.updatePassword(encodedPassword);
  }

  public List<ImageUrlResponse> getRetripHistoryByMemberId(String memberId) {

    List<RetripReport> retripReports = retripReportRepository.findByMemberId(memberId).orElseThrow(
        () -> new IllegalStateException("해당 회원의 Retrip 히스토리가 없습니다.")
    );

    return retripReports.stream()
        .map(report -> ImageUrlResponse.builder()
            .imageUrl(report.getImageUrl())
            .build()).toList();
  }
}
