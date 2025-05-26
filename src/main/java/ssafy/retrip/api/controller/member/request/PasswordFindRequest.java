package ssafy.retrip.api.controller.member.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.member.request.PasswordFindServiceRequest;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordFindRequest {

  @NotNull(message = "아이디는 필수 입력값입니다.")
  private String userId; // 회원 아이디

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;

  @NotNull(message = "코드는 필수 입력값입니다.")
  @Size(min = 6, max = 6, message = "코드는 6자리여야 합니다.")
  private String code;

  public PasswordFindServiceRequest toServiceRequest() {
    return PasswordFindServiceRequest.builder()
        .userId(userId)
        .email(email)
        .code(code)
        .build();
  }
}