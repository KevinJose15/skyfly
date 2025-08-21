package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Repositorios.IClienteRepository;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
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

    @Autowired
    private IUsuarioService usuarioService;

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
        // ⚡ Hidratar el usuario antes de guardar
        if (cliente.getUsuario() != null && cliente.getUsuario().getId() != null) {
            Usuario usuario = usuarioService.buscarPorId(cliente.getUsuario().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            cliente.setUsuario(usuario);
        }
        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminarPorId(Integer id) {
        clienteRepository.deleteById(id);
    }
}
