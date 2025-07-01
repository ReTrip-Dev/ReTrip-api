package ssafy.retrip.api.controller.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @GetMapping("/test")
  public String test() {
    return "CI Test Success!";
  }

  @GetMapping("/re-test")
  public String reTest() {
    return "CI Re-Test Success!";
  }
}
