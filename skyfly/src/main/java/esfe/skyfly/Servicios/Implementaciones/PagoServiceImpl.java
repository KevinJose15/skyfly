package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Repositorios.IPagoRepository;
import esfe.skyfly.Servicios.Interfaces.IPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;

@Service
public class PagoServiceImpl implements IPagoService {

    @Autowired
    private IPagoRepository pagoRepository;
    @Autowired
    private CodigoConfirmacionService CodigoConfirmacionServiceImpl;

    @Override
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }

    @Override
    public Pago crearOeditar(Pago pago) {
        return pagoRepository.save(pago);
    }

    @Override
    public void eliminarPorId(Integer id) {
        pagoRepository.deleteById(id);
}
// 🔹 Nuevo método: busca el último pago asociado al email del cliente
    @Override
    public Pago buscarPorEmailCliente(String email) {
        return pagoRepository.findUltimoPagoPorEmail(email)
                .orElse(null);
}
}