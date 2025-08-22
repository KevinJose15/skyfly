package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Repositorios.IPaqueteRepository;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaqueteService implements IPaqueteService {

    @Autowired
    private IPaqueteRepository paqueteRepository;

    @Override
    public List<Paquete> obtenerTodo() {
        return paqueteRepository.findAll();
    }

    @Override
    public Page<Paquete> buscarTodos(Pageable pageable) {
        return paqueteRepository.findAll(pageable);
    }

    @Override
    public Optional<Paquete> buscarPorId(Integer id) {
        return paqueteRepository.findById(id);
    }

    @Override
    public Paquete crearOeditar(Paquete paquete) {
        return paqueteRepository.save(paquete);
    }

    @Override
    public void eliminarPorId(Integer id) {
        paqueteRepository.deleteById(id);
    }

    // Eliminamos métodos duplicados
    @Override
    public List<Paquete> buscarTodos() {
        return paqueteRepository.findAll();
    }
}