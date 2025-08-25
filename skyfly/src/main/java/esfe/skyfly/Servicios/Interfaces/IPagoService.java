package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Pago;
import java.util.List;
import java.util.Optional;

public interface IPagoService {
    List<Pago> obtenerTodos();
    Optional<Pago> buscarPorId(Integer id);
    Pago crearOeditar(Pago pago);
    void eliminarPorId(Integer id);
}