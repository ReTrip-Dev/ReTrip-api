package ssafy.retrip.api.controller.member.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.member.request.PasswordResetServiceRequest;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetRequest {

  private String email;
  private String newPassword;

  public PasswordResetServiceRequest toServiceRequest() {
    return PasswordResetServiceRequest.builder()
        .email(email)
        .newPassword(newPassword)
        .build();
  }
}