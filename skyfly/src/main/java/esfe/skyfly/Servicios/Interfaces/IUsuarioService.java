package esfe.skyfly.Servicios.Interfaces;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import esfe.skyfly.Modelos.Rol;
import esfe.skyfly.Modelos.Usuario;

public interface IUsuarioService {
    Page<Usuario> buscarTodos(Pageable pageable);

    List<Usuario> obtenerTodos();

    Optional<Usuario> buscarPorId(Integer id);

    Usuario crearOeditar(Usuario usuario);

    void eliminarPorId(Integer id);
    
    List<Usuario> findByRol(Rol rol);

    String encodePassword(String passwordHash);

    // 👉 NUEVO: requerido por SecurityUtils para obtener el usuario logueado
    Optional<Usuario> buscarPorEmail(String email);
}
