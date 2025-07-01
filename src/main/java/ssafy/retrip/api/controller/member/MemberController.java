package ssafy.retrip.api.controller.member;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.retrip.api.controller.email.request.EmailRequest;
import ssafy.retrip.api.controller.email.request.EmailVerificationRequest;
import ssafy.retrip.api.controller.member.request.MemberSignInRequest;
import ssafy.retrip.api.controller.member.request.MemberSignUpRequest;
import ssafy.retrip.api.controller.member.request.PasswordFindRequest;
import ssafy.retrip.api.controller.member.request.PasswordResetRequest;
import ssafy.retrip.api.service.email.EmailService;
import ssafy.retrip.api.service.member.MemberService;
import ssafy.retrip.api.service.retrip.response.ImageUrlResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

  public final EmailService emailService;
  public final MemberService memberService;

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody MemberSignUpRequest request) {
    memberService.signup(request.toServiceRequest());
    return ResponseEntity.ok("success");
  }

  @PostMapping("/signin")
  public ResponseEntity<String> signin(@Valid @RequestBody MemberSignInRequest request) {
    memberService.signIn(request.toServiceRequest());
    return ResponseEntity.ok("로그인 성공");
  }

  @GetMapping("/validate/nickname")
  public ResponseEntity<String> validateNickname(
      @RequestParam(value = "nickname") String nickname) {
    memberService.validateNickname(nickname);
    return ResponseEntity.ok("사용 가능한 아이디입니다.");
  }

  @PostMapping("/send-verification-code")
  public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody EmailRequest request) {
    memberService.sendVerificationCode(request.getEmail());
    return ResponseEntity.ok("success");
  }

  @PostMapping("/find-id")
  public ResponseEntity<String> findUserId(@Valid @RequestBody EmailVerificationRequest request) {
    emailService.verifyEmailCode(request.toServiceRequest());
    String forgotUserId = memberService.getForgotUserId(request.getEmail());
    return ResponseEntity.ok(forgotUserId);
  }

  @PostMapping("/password/verify-credentials")
  public ResponseEntity<String> verifyPasswordResetCredentials(
      @Valid @RequestBody PasswordFindRequest request) {
    memberService.verifyPasswordResetCredentials(request.toServiceRequest());
    return ResponseEntity.ok("success");
  }

  @PostMapping("/password/reset")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
    memberService.resetPassword(request.toServiceRequest());
    return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
  }

  @GetMapping("/history")
  public ResponseEntity<List<ImageUrlResponse>> getRetripHistoryByMemberId() {

    String memberId = "4277332119";
    List<ImageUrlResponse> responses = memberService.getRetripHistoryByMemberId(memberId);

    return ResponseEntity.ok(responses);
  }
}
