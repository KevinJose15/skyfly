package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.CodigoConfirmacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CodigoConfirmacionRepository extends JpaRepository<CodigoConfirmacion, Integer> {

    // Orden descendente por idCodigo (que coincide con id_codigo de la BD)
    Optional<CodigoConfirmacion> findTopByEmailOrderByIdCodigoDesc(String email);

    Optional<CodigoConfirmacion> findByEmailAndCodigo(String email, String codigo);
}