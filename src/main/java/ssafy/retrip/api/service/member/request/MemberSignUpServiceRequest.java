package ssafy.retrip.api.service.member.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberSignUpServiceRequest {


  private String userId;

  private String password;

  private String email;

  @Builder
  private MemberSignUpServiceRequest(String userId, String password, String email) {
    this.userId = userId;
    this.password = password;
    this.email = email;
  }
}