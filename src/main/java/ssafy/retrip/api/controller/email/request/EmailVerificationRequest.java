package ssafy.retrip.api.controller.email.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import ssafy.retrip.api.service.email.request.EmailVerificationServiceRequest;

@Getter
public class EmailVerificationRequest {

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;

  private String code;

  public EmailVerificationServiceRequest toServiceRequest() {
    return EmailVerificationServiceRequest.builder()
        .email(email)
        .code(code).build();
  }
}