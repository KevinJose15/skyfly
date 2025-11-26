package esfe.skyfly.Servicios.Implementaciones;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Repositorios.IUsuarioRepository;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
import esfe.skyfly.Modelos.Rol;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<Usuario> buscarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional
    public Usuario crearOeditar(Usuario usuario) {
        if (usuario.getId() == null) {
            // Nuevo usuario → siempre encriptamos contraseña
            usuario.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
            return usuarioRepository.save(usuario);
        } else {
            // Edición
            return usuarioRepository.findById(usuario.getId()).map(usuarioExistente -> {
                usuarioExistente.setName(usuario.getName());
                usuarioExistente.setEmail(usuario.getEmail());
                usuarioExistente.setRol(usuario.getRol());
                usuarioExistente.setStatus(usuario.getStatus());

                if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank()) {
                    usuarioExistente.setPasswordHash(passwordEncoder.encode(usuario.getPasswordHash()));
                }

                return usuarioRepository.save(usuarioExistente);
            }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuario.getId()));
        }
    }
     @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return usuarioRepository.findByEmailIgnoreCase(email.trim());
    }
     @Override
    public boolean emailExiste(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public void eliminarPorId(Integer id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<Usuario> findByRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    @Override
    public String encodePassword(String passwordHash) {
        return passwordEncoder.encode(passwordHash);
    }

    // ✅ Implementación de Spring Security
 @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return User.builder()
                .username(usuario.getEmail())  // usamos email como username
                .password(usuario.getPasswordHash()) // contraseña ya encriptada
                .roles(usuario.getRol().name()) // asignamos el rol
                .build();
    }
}
