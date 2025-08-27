package esfe.skyfly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
          .authorizeHttpRequests(auth -> auth
              // -------------------------
              // Público (sin login)
              // -------------------------
              .requestMatchers(
                  "/", "/bienvenida", "/login", "/registro",
                  "/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**", "/favicon.ico"
              ).permitAll()

              // -------------------------
              // CLIENTE (poner ANTES que backoffice para que no lo pisen los catch-alls)
              // -------------------------
    .requestMatchers(
  "/destinos/index-cliente",
  "/paquetes/index-cliente",
  "/reservas/index-cliente", "/reservas/create-cliente",
  "/pagos/index-cliente", "/pagos/create-cliente",
  "/codigo/**"
).hasRole("CLIENTE")

              // -------------------------
              // ADMIN-ONLY
              // -------------------------
              .requestMatchers(
                  "/usuarios/**"
              ).hasRole("ADMINISTRADOR")

              // -------------------------
              // BACKOFFICE (ADMIN + AGENTE)
              // Nota: estas rutas incluyen CRUD y listados operativos.
              // Las rutas de cliente ya quedaron protegidas arriba y no se pisan por el orden.
              // -------------------------
              .requestMatchers(
                  "/clientes/**",
                  "/destinos/**",
                  "/paquetes/**",
                  "/reservas/**",
                  "/pagos/**",
                  "/metodopago/**",
                  "/facturas/**"
              ).hasAnyRole("ADMINISTRADOR", "AGENTE")

              // Cualquier otra cosa requiere autenticación
              .anyRequest().authenticated()
          )
          .formLogin(form -> form
              .loginPage("/login")
              .usernameParameter("email")
              .passwordParameter("password")
              .successHandler(successHandler) // redirección por rol
              .failureUrl("/login?error=true")
              .permitAll()
          )
          .logout(logout -> logout
              .logoutUrl("/logout")
              .logoutSuccessUrl("/login?logout")
              .permitAll()
          )
          .exceptionHandling(ex -> ex
              .accessDeniedPage("/access-denied")
          );

        // CSRF habilitado por defecto (OK para formularios con token)
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
