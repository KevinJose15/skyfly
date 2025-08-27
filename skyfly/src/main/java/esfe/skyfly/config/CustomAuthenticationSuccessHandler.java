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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectURL = request.getContextPath();

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();

            if ("Administrador".equals(role)) {
                redirectURL = "/admin/main";   // _MainLayout.html
                break;
            } else if ("Agente".equals(role)) {
                redirectURL = "/agente/main";  // _MainLayout.html
                break;
            } else if ("Cliente".equals(role)) {
                redirectURL = "/cliente/index"; // Cliente/index.html
                break;
            }
        }

        response.sendRedirect(redirectURL);
    }
}
