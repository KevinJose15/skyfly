package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import esfe.skyfly.Modelos.EstadoReserva;

import java.util.Optional;

public interface IPagoRepository extends JpaRepository<Pago, Integer>{ 

    // Busca el último pago PENDIENTE del cliente (ordenado por fecha)
Optional<Pago> findFirstByReserva_Cliente_Usuario_EmailAndEstadoPagoOrderByFechaPagoDesc(
        String email,
        EstadoReserva estadoPago
);


}