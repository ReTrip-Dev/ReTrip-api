package ssafy.retrip.api.controller.member.request;

import lombok.Getter;
import ssafy.retrip.api.service.member.request.MemberSignInServiceRequest;

@Getter
public class MemberSignInRequest {

  private String userId;
  private String password;

  public MemberSignInServiceRequest toServiceRequest() {
    return MemberSignInServiceRequest.builder()
        .userId(userId)
        .password(password)
        .build();
  }
}