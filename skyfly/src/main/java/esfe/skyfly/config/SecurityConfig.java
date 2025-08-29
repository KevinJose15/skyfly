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

                // Portal Cliente (B2C)
                .requestMatchers("/cliente/**").hasRole("Cliente")

                // Pagos y Código: abiertos a Cliente, Agente y Administrador
                // 👇 IMPORTANTE: antes que el bloque de Agente
                .requestMatchers("/pagos/**", "/codigo", "/codigo/**","/facturas/**")
                    .hasAnyRole("Cliente", "Agente", "Administrador")

                // Administración: Usuarios y Clientes
                .requestMatchers("/usuarios/**", "/clientes/**").hasRole("Administrador")

                // Back-office Agente (sin /pagos ni /codigo aquí)
                .requestMatchers("/destinos/**", "/paquetes/**", "/reservas/**",
                                 "/metodopago/**")
                    .hasRole("Agente")

                // Resto autenticado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler) // usamos tu success handler
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        // CSRF habilitado por defecto (Spring Security 6)
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}