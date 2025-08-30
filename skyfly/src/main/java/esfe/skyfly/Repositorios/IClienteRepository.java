package esfe.skyfly.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Rol;

public interface IClienteRepository extends JpaRepository<Cliente, Integer> {
    List<Cliente> findByUsuario_Rol(Rol rol);
    
    // Cliente por el email del Usuario (anidado)
    Optional<Cliente> findByUsuario_Email(String email);
}