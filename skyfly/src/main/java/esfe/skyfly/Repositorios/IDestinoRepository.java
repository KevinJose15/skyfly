package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Destino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDestinoRepository extends JpaRepository<Destino, Integer> {
}

