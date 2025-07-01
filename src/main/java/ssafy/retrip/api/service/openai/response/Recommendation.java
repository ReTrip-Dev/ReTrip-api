package ssafy.retrip.api.service.openai.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Recommendation {

  private String emoji;
  private String place;
  private String description;
}