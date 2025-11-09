package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Destino;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDestinoRepository extends JpaRepository<Destino, Integer> {
    Page<Destino> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
        String nombre, String descripcion, Pageable pageable);
}
