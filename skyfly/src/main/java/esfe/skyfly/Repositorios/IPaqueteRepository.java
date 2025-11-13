package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IPaqueteRepository extends JpaRepository<Paquete, Integer> {

    List<Paquete> findByDestino_DestinoId(Integer destinoId);

    @Query("select min(p.precio) from Paquete p where p.destino.destinoId = :destinoId")
    BigDecimal findMinPrecioByDestinoId(@Param("destinoId") Integer destinoId);
}
