package ssafy.retrip.api.controller.member;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssafy.retrip.api.controller.email.request.EmailRequest;
import ssafy.retrip.api.controller.email.request.EmailVerificationRequest;
import ssafy.retrip.api.controller.member.request.MemberSignInRequest;
import ssafy.retrip.api.controller.member.request.MemberSignUpRequest;
import ssafy.retrip.api.service.email.EmailService;
import ssafy.retrip.api.service.member.MemberService;

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

  @PostMapping("/find-id/send-code")
  public ResponseEntity<String> findForgetUserId(@Valid @RequestBody EmailRequest request) {
    memberService.findForgotUserId(request.getEmail());
    return ResponseEntity.ok("success");
  }

  @PostMapping("/find-id")
  public ResponseEntity<String> find(@Valid @RequestBody EmailVerificationRequest request) {
    emailService.verifySignUpEmailCode(request.toServiceRequest());
    String forgotUserId = memberService.getForgotUserId(request.getEmail());
    return ResponseEntity.ok(forgotUserId);
  }


  // TODO: 비밀번호 찾기
  @PostMapping("/find-pw")
  public ResponseEntity<String> findUserPw() {

    memberService.findForgotUserPw();

    return ResponseEntity.ok("success");
  }

  // TODO: 회원 리트립 히스토리 조회
  @GetMapping("/history/{memberId}")
  public void getHistory(@PathVariable Long memberId) {

  }
}
