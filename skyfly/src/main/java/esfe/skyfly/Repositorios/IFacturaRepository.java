package esfe.skyfly.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Modelos.Reservas;

public interface IFacturaRepository extends JpaRepository<Factura, Integer> {
    List<Factura> findByReserva(Reservas reserva);
    Optional<Factura> findFirstByReservaOrderByIdFacturaDesc(Reservas reserva);
    boolean existsByReserva(Reservas reserva);

    // Para el índice: trae 'reserva' y evita lazy en la tabla
    @Override
    @EntityGraph(attributePaths = {
        "reserva"
    })
    List<Factura> findAll();
    @Override
    @EntityGraph(attributePaths = {
            "reserva",
            "reserva.cliente",
            "reserva.cliente.usuario",
            "reserva.paquete",
            "reserva.paquete.destino"
    })
    Optional<Factura> findById(Integer id);
}