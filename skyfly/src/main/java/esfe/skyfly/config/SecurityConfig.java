package esfe.skyfly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {   

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/bienvenida", "/login", "/css/**", "/js/**", "/images/**").permitAll() // <- públicas
                .anyRequest().authenticated() // lo demás requiere login
            )
.formLogin(form -> form
    .loginPage("/login")
    .usernameParameter("email")
    .passwordParameter("password")
    .defaultSuccessUrl("/home", true) // 👈 después de login manda a /home
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


