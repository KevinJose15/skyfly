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

            if (role.equals("Administrador")) {
                // 👉 Vista para administrador
                redirectURL = "/admin/main";
                break;
            } else if (role.equals("Agente")) {
                // 👉 Vista para agente
                redirectURL = "/agente/main";
                break;
            } else if (role.equals("Cliente")) {
                // 👉 Vista para cliente
                redirectURL = "/cliente/index";
                break;
            }
        }

        response.sendRedirect(redirectURL);
    }
}