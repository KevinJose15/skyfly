package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByUsuario_Rol(Rol rol);
}
