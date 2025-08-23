package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.Reservas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IReservaService {

    List<Reservas> obtenerTodos();

 

    Page<Reservas> buscarTodos(Pageable pageable);

    Optional<Reservas> buscarPorId(Integer id);

    Reservas crearOeditar(Reservas reserva);

    void eliminarPorId(Integer id);


}
