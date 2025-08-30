package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IPaqueteRepository extends JpaRepository<Paquete, Integer> {
      List<Paquete> findByDestino_DestinoId(Integer destinoId);
}