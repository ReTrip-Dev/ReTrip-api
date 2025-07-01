package ssafy.retrip.api.controller.email;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ssafy.retrip.api.controller.email.request.EmailRequest;
import ssafy.retrip.api.controller.email.request.EmailVerificationRequest;
import ssafy.retrip.api.service.email.EmailService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

  private final EmailService emailService;

  @PostMapping
  public ResponseEntity<String> sendSignUpEmailCode(@Valid @RequestBody EmailRequest request) {
    emailService.joinEmail(request.getEmail());
    return ResponseEntity.ok("success");
  }

  @PostMapping("/verify")
  public ResponseEntity<String> verifySignUpEmailCode(
      @Valid @RequestBody EmailVerificationRequest request) {

    emailService.verifyEmailCode(request.toServiceRequest());
    return ResponseEntity.ok("success");
  }
}