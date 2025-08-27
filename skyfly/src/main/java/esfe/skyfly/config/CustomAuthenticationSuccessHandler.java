package esfe.skyfly.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private boolean hasRole(Authentication auth, String rolKey) {
    return auth.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .anyMatch(a -> a.equals("ROLE_" + rolKey) || a.equals(rolKey));
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    String redirectURL = request.getContextPath();

    if (hasRole(authentication, "ADMINISTRADOR")) {
      redirectURL = "/admin/main";
    } else if (hasRole(authentication, "AGENTE")) {
      redirectURL = "/agente/main";
    } else if (hasRole(authentication, "CLIENTE")) {
      redirectURL = "/cliente/index";
    } else {
      redirectURL = "/bienvenida";
    }
    response.sendRedirect(redirectURL);
  }
}