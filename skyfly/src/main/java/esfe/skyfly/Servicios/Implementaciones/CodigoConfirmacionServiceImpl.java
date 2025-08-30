package esfe.skyfly.Servicios.Implementaciones;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Modelos.CodigoConfirmacion;
import esfe.skyfly.Repositorios.CodigoConfirmacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class CodigoConfirmacionServiceImpl implements CodigoConfirmacionService {

    private final CodigoConfirmacionRepository codigoRepo;

    public CodigoConfirmacionServiceImpl(CodigoConfirmacionRepository codigoRepo) {
        this.codigoRepo = codigoRepo;
    }

    @Override
    public CodigoConfirmacion crearCodigo(String email) {
        CodigoConfirmacion codigo = new CodigoConfirmacion();
        codigo.setEmail(email);

        // Código aleatorio de 6 dígitos
        String codigoGenerado = String.format("%06d", new Random().nextInt(999999));
        codigo.setCodigo(codigoGenerado);

        codigo.setFechaGeneracion(LocalDateTime.now());
        codigo.setUsado(false);

        return codigoRepo.save(codigo);
    }

    @Override
    public boolean validarCodigo(String email, String codigo) {
        Optional<CodigoConfirmacion> opt = codigoRepo.findByEmailAndCodigo(email, codigo);
        if (opt.isPresent()) {
            CodigoConfirmacion encontrado = opt.get();
            if (!encontrado.isUsado()) {
                encontrado.setUsado(true);
                codigoRepo.save(encontrado);
                return true;
            }
        }
        return false;
    }
}