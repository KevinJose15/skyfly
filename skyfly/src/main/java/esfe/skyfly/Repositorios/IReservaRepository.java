package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IReservaRepository extends JpaRepository<Reservas, Integer> {
    
   
}
