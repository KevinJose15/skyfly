package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Repositorios.IDestinoRepository;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinoService implements IDestinoService {

    @Autowired
    private IDestinoRepository destinoRepository;

    @Override
    public List<Destino> buscarTodos() {
        return destinoRepository.findAll();
    }

    @Override
    public Page<Destino> buscarTodos(Pageable pageable) {
        return destinoRepository.findAll(pageable);
    }

    @Override
    public Optional<Destino> buscarPorId(Integer id) {
        return destinoRepository.findById(id);
    }

    @Override
    public Destino crearOeditar(Destino destino) {
        return destinoRepository.save(destino);
    }

    @Override
    public void eliminarPorId(Integer id) {
        destinoRepository.deleteById(id);
    }
}
