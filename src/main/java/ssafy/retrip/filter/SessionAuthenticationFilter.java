package ssafy.retrip.filter;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SessionAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    HttpSession session = request.getSession();
//    String kakaoId = (String) session.getAttribute("member");
//
//    if (isEmpty(kakaoId)) {
//      response.sendRedirect("/login");
//      return;
//    }

    filterChain.doFilter(request, response);
  }
}
