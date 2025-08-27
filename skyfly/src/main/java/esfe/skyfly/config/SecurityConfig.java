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
                // Públicas
                .requestMatchers("/", "/bienvenida", "/login", "/registro",
                                 "/css/**", "/js/**", "/images/**").permitAll()

                // ADMIN (gestión completa de usuarios)
                .requestMatchers("/usuarios/**").hasAuthority("Administrador")

                // ADMIN + AGENTE (CRUD de negocio)
                .requestMatchers("/clientes/mant/**").hasAnyAuthority("Administrador", "Agente")
                .requestMatchers("/destinos/mant/**").hasAnyAuthority("Administrador", "Agente")
                .requestMatchers("/paquetes/mant/**").hasAnyAuthority("Administrador", "Agente")
                .requestMatchers("/reservas/mant/**").hasAnyAuthority("Administrador", "Agente")
                .requestMatchers("/pagos/mant/**").hasAnyAuthority("Administrador", "Agente")

                // CLIENTE (solo lectura + su información)
                .requestMatchers("/cliente/**").hasAuthority("Cliente")
                .requestMatchers("/destinos/index/**").hasAuthority("Cliente")
                .requestMatchers("/paquetes/index/**").hasAuthority("Cliente")
                .requestMatchers("/reservas/index/**").hasAuthority("Cliente")
                .requestMatchers("/pagos/index/**").hasAuthority("Cliente")

                // Resto autenticado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)   // ← redirección por rol
                .failureUrl("/login?error=true")
                .permitAll()
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
