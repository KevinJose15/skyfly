package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Repositorios.IClienteRepository;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService implements IClienteService {

    @Autowired
    private IClienteRepository clienteRepository;

    @Override
    public Page<Cliente> buscarTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Cliente crearOeditar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminarPorId(Integer id) {
        clienteRepository.deleteById(id);
    }

    // ✅ NUEVO: para resolver SecurityUtils.getClienteActual()
    @Override
    public Optional<Cliente> buscarPorUsuarioId(Integer usuarioId) {
        return clienteRepository.findByUsuario_Id(usuarioId);
    }
}
