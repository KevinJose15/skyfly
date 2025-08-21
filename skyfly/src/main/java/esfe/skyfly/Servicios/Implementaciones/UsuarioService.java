 package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.SkyflyApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Repositorios.IUsuarioRepository;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
import esfe.skyfly.Modelos.Rol;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService implements IUsuarioService {

    private final SkyflyApplication skyflyApplication;

    @Autowired
    private IUsuarioRepository usuarioRepository;
    
    UsuarioService(SkyflyApplication skyflyApplication) {
        this.skyflyApplication = skyflyApplication;
    }

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
        if (usuario.getId() != null) {
            // Edición: cargar el usuario existente
            Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findById(usuario.getId());
            if (usuarioExistenteOpt.isPresent()) {
                Usuario usuarioExistente = usuarioExistenteOpt.get();

                // ⚡ Mantener la contraseña si no se envió una nueva
                if (usuario.getPasswordHash() != null && !usuario.getPasswordHash().isBlank()) {
                    usuarioExistente.setPasswordHash(usuario.getPasswordHash());
                }

                usuarioExistente.setName(usuario.getName());
                usuarioExistente.setEmail(usuario.getEmail());
                usuarioExistente.setRol(usuario.getRol());
                usuarioExistente.setStatus(usuario.getStatus());

                return usuarioRepository.save(usuarioExistente);
            } else {
                throw new RuntimeException("Usuario no encontrado para edición");
            }
        } else {
            // Creación
            return usuarioRepository.save(usuario);
        }
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodePassword'");
    }
}
