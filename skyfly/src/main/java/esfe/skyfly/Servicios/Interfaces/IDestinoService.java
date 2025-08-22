package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Modelos.Paquete;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IDestinoService {
    List<Destino> buscarTodos();
    List<Destino> obtenerTodo();
    Page<Destino> buscarTodos(Pageable pageable);
    Optional<Destino> buscarPorId(Integer id);
    Destino crearOeditar(Destino destino);
    void eliminarPorId(Integer id);
}

