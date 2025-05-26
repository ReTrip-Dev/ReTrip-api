package ssafy.retrip.api.service.member.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PasswordFindServiceRequest {

  @NotNull(message = "아이디는 필수 입력값입니다.")
  private String userId; // 회원 아이디

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;

  @NotNull(message = "코드는 필수 입력값입니다.")
  @Size(min = 6, max = 6, message = "코드는 6자리여야 합니다.")
  private String code;

  @Builder
  private PasswordFindServiceRequest(String userId, String email, String code) {
    this.userId = userId;
    this.email = email;
    this.code = code;
  }
}