package ssafy.retrip.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ssafy.retrip.api.service.oauth.CustomOAuth2UserService;
import ssafy.retrip.filter.SessionAuthenticationFilter;
import ssafy.retrip.handler.OAuth2AuthenticationFailureHandler;
import ssafy.retrip.handler.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final SessionAuthenticationFilter sessionAuthenticationFilter;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .formLogin(FormLoginConfigurer::disable)
        .logout(LogoutConfigurer::disable);

    http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().permitAll()
        );

    http
        .oauth2Client(Customizer.withDefaults())
        .oauth2Login(oauth2 -> oauth2
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
            .userInfoEndpoint(userInfo -> userInfo
                .oidcUserService(customOAuth2UserService)
            )
        );

    http
        .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    http
        .sessionManagement(
            session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .expiredUrl("/login?expired=true")
        );

    return http.build();
  }
}
