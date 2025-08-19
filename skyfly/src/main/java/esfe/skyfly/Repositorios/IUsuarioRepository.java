package esfe.skyfly.Repositorios;
import org.springframework.data.jpa.repository.JpaRepository;   
import esfe.skyfly.Modelos.Usuario;
public interface IUsuarioRepository extends JpaRepository<Usuario, Integer> {


}
