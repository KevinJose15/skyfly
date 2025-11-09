package esfe.skyfly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // <-- IMPORTANTE

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationSuccessHandler successHandler;

    public SecurityConfig(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Público
                .requestMatchers("/", "/bienvenida", "/login", "/registro", "/error",
                                 "/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**", "/favicon.ico")
                    .permitAll()

                // 🔓 Travel Copilot (Gemini) público
                .requestMatchers("/api/ai/**").permitAll()

                // Portal Cliente (B2C)
                .requestMatchers("/cliente/**").hasRole("Cliente")

                // Pagos y Código: Cliente/Agente/Admin
                .requestMatchers("/pagos/**", "/codigo", "/codigo/**", "/facturas/**")
                    .hasAnyRole("Cliente", "Agente", "Administrador")

                // Administración: Usuarios y Clientes
                .requestMatchers("/usuarios/**", "/clientes/**").hasRole("Administrador")

                // Back-office Agente
                .requestMatchers("/destinos/**", "/paquetes/**", "/reservas/**", "/metodopago/**")
                    .hasRole("Agente")

                // Resto autenticado
                .anyRequest().authenticated()
            )
            // ❗ Ignoramos CSRF solo para /api/ai/** (POST desde fetch)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/ai/**"))
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
