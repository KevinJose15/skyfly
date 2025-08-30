package esfe.skyfly.Servicios.Interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import esfe.skyfly.Modelos.Cliente;

public interface IClienteService {

    Page<Cliente> buscarTodos(Pageable pageable);

    List<Cliente> obtenerTodos();

    Optional<Cliente> buscarPorId(Integer id);

    Cliente crearOeditar(Cliente cliente);

    void eliminarPorId(Integer id);
   
    Cliente buscarPorUsuarioEmail(String email);

}