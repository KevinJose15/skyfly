package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    // CRUD estándar (ajusta según lo que ya tengas)
    Page<Cliente> buscarTodos(Pageable pageable);
    List<Cliente> obtenerTodos();
    Optional<Cliente> buscarPorId(Integer id);
    Cliente crearOeditar(Cliente cliente);
    void eliminarPorId(Integer id);

    // 👉 NUEVO: requerido por SecurityUtils
    Optional<Cliente> buscarPorUsuarioId(Integer usuarioId);
}
