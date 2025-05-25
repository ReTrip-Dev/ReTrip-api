package ssafy.retrip.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

  @Value("${spring.mail.username}")
  private String username;

  @Value("${spring.mail.password}")
  private String password;

  @Value("${spring.mail.host}")
  private String host;

  @Value("${spring.mail.port}")
  private int port;

  @Bean
  public JavaMailSender mailSender() {

    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);
    mailSender.setDefaultEncoding("UTF-8");

    Properties javaMailProperties = new Properties();
    javaMailProperties.put("mail.transport.protocol", "smtp");
    javaMailProperties.put("mail.smtp.auth", "true");
    javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    javaMailProperties.put("mail.smtp.starttls.enable", "true");
    javaMailProperties.put("mail.debug", "true");
    javaMailProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    javaMailProperties.put("mail.smtp.ssl.protocols", "TLSv1.3");

    javaMailProperties.put("mail.smtp.timeout", "5000");
    javaMailProperties.put("mail.smtp.connectiontimeout", "5000");
    javaMailProperties.put("mail.smtp.writetimeout", "5000");

    mailSender.setJavaMailProperties(javaMailProperties);

    return mailSender;
  }
}