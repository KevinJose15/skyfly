package esfe.skyfly.Servicios.Implementaciones;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Repositorios.IUsuarioRepository;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
import esfe.skyfly.SkyflyApplication;

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
public Usuario crearOeditar(Usuario usuario) {
    if (usuario.getId() == null) {
        // CREACIÓN
       if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
    throw new IllegalArgumentException("La contraseña es requerida");
}
        if (usuario.getStatus() == null) {
            usuario.setStatus(true);
        }
    } else {
        // EDICIÓN
        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario con id " + usuario.getId() + " no existe"));

        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
            usuario.setPasswordHash(existente.getPasswordHash());
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            usuario.setEmail(existente.getEmail());
        }
        if (usuario.getStatus() == null) {
            usuario.setStatus(existente.getStatus());
        }
        if (usuario.getRol() == null) {
            usuario.setRol(existente.getRol());
        }
    }
    return usuarioRepository.save(usuario);
}
    @Override
    public void eliminarPorId(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
