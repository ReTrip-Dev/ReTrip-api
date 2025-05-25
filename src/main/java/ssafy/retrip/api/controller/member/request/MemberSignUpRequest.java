package ssafy.retrip.api.controller.member.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.retrip.api.service.member.request.MemberSignUpServiceRequest;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSignUpRequest {

  @NotNull(message = "아이디는 필수 입력값입니다.")
  private String userId;

  @NotNull(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
  private String password;

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;

  @Builder
  private MemberSignUpRequest(String userId, String password, String email) {
    this.userId = userId;
    this.password = password;
    this.email = email;
  }

  public MemberSignUpServiceRequest toServiceRequest() {
    return MemberSignUpServiceRequest.builder()
        .userId(userId)
        .password(password)
        .email(email).build();
  }
}