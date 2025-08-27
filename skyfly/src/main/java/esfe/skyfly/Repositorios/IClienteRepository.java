package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IClienteRepository extends JpaRepository<Cliente, Integer> {

    // Consulta por la FK de usuario: asumiendo Cliente.usuario (ManyToOne) → Usuario.id
    Optional<Cliente> findByUsuario_Id(Integer usuarioId);
}
