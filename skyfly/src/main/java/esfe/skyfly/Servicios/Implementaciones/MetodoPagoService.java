package esfe.skyfly.Servicios.Implementaciones;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import esfe.skyfly.Modelos.MetodoPago;
import esfe.skyfly.Repositorios.IMetodoPagoRepository;
import esfe.skyfly.Servicios.Interfaces.IMetodoPagoService;
import esfe.skyfly.SkyflyApplication;

@Service
public class MetodoPagoService implements IMetodoPagoService {

    private final SkyflyApplication skyflyApplication;

    @Autowired
    private IMetodoPagoRepository metodoPagoRepository;

    MetodoPagoService(SkyflyApplication skyflyApplication) {
        this.skyflyApplication = skyflyApplication;
    }

    @Override
    public Page<MetodoPago> buscarTodos(Pageable pageable) {
        return metodoPagoRepository.findAll(pageable);
    }

    @Override
    public List<MetodoPago> obtenerTodos() {
        return metodoPagoRepository.findAll();
    }

    @Override
    public Optional<MetodoPago> buscarPorId(Integer id) {
        return metodoPagoRepository.findById(id);
    }

    @Override
    public void eliminarPorId(Integer id) {
        metodoPagoRepository.deleteById(id);
    }
    @Override
    public MetodoPago crearOeditar(MetodoPago metodoPago) {
        return metodoPagoRepository.save(metodoPago);
    }
}