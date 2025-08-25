package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IPagoRepository extends JpaRepository<Pago, Integer>{ 

@Query("SELECT p FROM Pago p " +
           "WHERE p.reserva.cliente.usuario.email = :email " +
           "ORDER BY p.fechaPago DESC")
    Optional<Pago> findUltimoPagoPorEmail(@Param("email") String email);
}