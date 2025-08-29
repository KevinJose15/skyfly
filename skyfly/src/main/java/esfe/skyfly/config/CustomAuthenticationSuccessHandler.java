package esfe.skyfly.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        for (GrantedAuthority ga : authentication.getAuthorities()) {
            String auth = ga.getAuthority(); // "ROLE_Administrador" | "ROLE_Agente" | "ROLE_Cliente"
            if ("ROLE_Administrador".equals(auth)) {
                response.sendRedirect("/admin/main");
                return;
            }
            if ("ROLE_Agente".equals(auth)) {
                response.sendRedirect("/agente/main");
                return;
            }
            if ("ROLE_Cliente".equals(auth)) {
                response.sendRedirect("/cliente/destinos"); // landing B2C (lo creamos en el siguiente paso)
                return;
            }
        }
        // Fallback
        response.sendRedirect("/bienvenida");
    }
}