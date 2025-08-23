package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Modelos.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByRol(Rol rol); // 👈 así debe estar
}
