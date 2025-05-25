package ssafy.retrip.api.controller.email.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EmailRequest {

  @NotNull(message = "이메일은 필수 입력값입니다.")
  private String email;
}