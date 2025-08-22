package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Paquete;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IPaqueteService {

    List<Paquete> obtenerTodo();

    List<Paquete> buscarTodos();

    Page<Paquete> buscarTodos(Pageable pageable);

    Optional<Paquete> buscarPorId(Integer id);

    Paquete crearOeditar(Paquete paquete);

    void eliminarPorId(Integer id);
}