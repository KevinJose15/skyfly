package esfe.skyfly.Servicios.Interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import esfe.skyfly.Modelos.MetodoPago;

public interface IMetodoPagoService {
    Page<MetodoPago> buscarTodos(Pageable pageable);

    List<MetodoPago> obtenerTodos();

    Optional<MetodoPago> buscarPorId(Integer id);

    MetodoPago crearOeditar(MetodoPago metodoPago);

    void eliminarPorId(Integer id);
}