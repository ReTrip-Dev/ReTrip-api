package ssafy.retrip.api.service.member.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberSignInServiceRequest {

  private String userId;
  private String password;

  @Builder
  private MemberSignInServiceRequest(String userId, String password) {
    this.userId = userId;
    this.password = password;
  }
}