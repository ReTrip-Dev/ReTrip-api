package ssafy.retrip.api.service.member.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetServiceRequest {

  private String email;
  private String newPassword;

  @Builder
  private PasswordResetServiceRequest(String email, String newPassword) {
    this.email = email;
    this.newPassword = newPassword;
  }
}