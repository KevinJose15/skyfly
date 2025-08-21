package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Modelos.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {
    List<Usuario> findByRol(Rol rol); // 👈 así debe estar
}
