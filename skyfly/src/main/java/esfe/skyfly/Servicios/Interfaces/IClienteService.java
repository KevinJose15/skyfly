package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IClienteService {

    Page<Cliente> buscarTodos(Pageable pageable);

    List<Cliente> obtenerTodos();

    Optional<Cliente> buscarPorId(Integer id);

    Cliente crearOeditar(Cliente cliente);

    void eliminarPorId(Integer id);

}
