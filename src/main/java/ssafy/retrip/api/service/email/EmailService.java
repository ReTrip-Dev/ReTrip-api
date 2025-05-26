package ssafy.retrip.api.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ssafy.retrip.api.service.email.request.EmailVerificationServiceRequest;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final int CODE_LENGTH = 6;

  private final JavaMailSender mailSender;
  private final RedisTemplate<String, String> redisTemplate;

  @Value("${spring.mail.username}")
  private String senderEmail;

  public void joinEmail(String userEmail) {
    String code = createRandomCode();
    String title = "[ReTrip] 회원가입 이메일 인증번호 발송";
    String content = "<p>ReTrip 회원가입을 위한 인증번호입니다.</p>"
                     + "<h3 style='color: " + "var(--main-color)" + ";'>" + code + "</h3>"
                     + "<p>인증번호는 3분간 유효합니다.</p>"
                     + "<p>감사합니다.</p>";
    sendEmailCode(userEmail, title, content, code);
  }

  public void findForgotUserId(String userEmail) {
    String code = createRandomCode();
    String title = "[ReTrip] 아이디 찾기 이메일 인증번호 발송";
    String content = "<p>ReTrip 아이디 찾기를 위한 인증번호입니다.</p>"
                     + "<h3 style='color: " + "var(--main-color)" + ";'>" + code + "</h3>"
                     + "<p>인증번호는 3분간 유효합니다.</p>"
                     + "<p>감사합니다.</p>";
    sendEmailCode(userEmail, title, content, code);
  }

  private void sendEmailCode(String userEmail, String title, String content, String code) {

    MimeMessage message = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(senderEmail);
      helper.setTo(userEmail);
      helper.setSubject(title);
      helper.setText(content, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      return;
    }

    ValueOperations<String, String> valOperations = redisTemplate.opsForValue();
    valOperations.set(userEmail, code, 180, TimeUnit.SECONDS);
  }

  public void verifySignUpEmailCode(EmailVerificationServiceRequest request) {
    ValueOperations<String, String> valOperations = redisTemplate.opsForValue();
    String code = valOperations.get(request.getEmail());
    if (!StringUtils.equals(code, request.getCode())) {
      throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
    }

    redisTemplate.delete(request.getEmail());
  }

  private String createRandomCode() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder sb = new StringBuilder(CODE_LENGTH);
    Random random = new Random();
    for (int i = 0; i < CODE_LENGTH; i++) {
      sb.append(chars.charAt(random.nextInt(chars.length())));
    }
    return sb.toString();
  }
}
