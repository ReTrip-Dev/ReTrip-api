package ssafy.retrip.api.controller.email.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import ssafy.retrip.api.service.email.request.EmailVerificationServiceRequest;

@Getter
public class EmailVerificationRequest {

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;

  @NotNull(message = "코드는 필수 입력값입니다.")
  @Size(min = 6, max = 6, message = "코드는 6자리여야 합니다.")
  private String code;

  public EmailVerificationServiceRequest toServiceRequest() {
    return EmailVerificationServiceRequest.builder()
        .email(email)
        .code(code).build();
  }
}