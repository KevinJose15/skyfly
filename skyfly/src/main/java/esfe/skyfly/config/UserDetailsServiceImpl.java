package esfe.skyfly.config;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Repositorios.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
Usuario usuario = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

boolean enabled = Boolean.TRUE.equals(usuario.getStatus()); // adapta según tipo

return new org.springframework.security.core.userdetails.User(
    usuario.getEmail(),
    usuario.getPasswordHash(),   // <-- hash que está en BD
    enabled,
    true, true, true,
    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
);
    }
}
