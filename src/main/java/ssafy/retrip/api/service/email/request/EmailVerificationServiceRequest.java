package ssafy.retrip.api.service.email.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class EmailVerificationServiceRequest {

  private String email;
  private String code;

  @Builder
  private EmailVerificationServiceRequest(String email, String code) {
    this.email = email;
    this.code = code;
  }
}