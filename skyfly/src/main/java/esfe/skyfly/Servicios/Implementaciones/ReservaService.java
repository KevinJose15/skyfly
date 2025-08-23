
package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Repositorios.IReservaRepository;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservaService implements IReservaService {

    @Autowired
    private IReservaRepository reservaRepository;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IPaqueteService paqueteService;

    @Override
    public Page<Reservas> buscarTodos(Pageable pageable) {
        return reservaRepository.findAll(pageable);
    }

    @Override
    public List<Reservas> obtenerTodos() {
        return reservaRepository.findAll();
    }

    @Override
    public Optional<Reservas> buscarPorId(Integer id) {
        return reservaRepository.findById(id);
    }

    @Override
    public Reservas crearOeditar(Reservas reserva) {
        // ⚡ Hidratar Cliente antes de guardar
        if (reserva.getCliente() != null && reserva.getCliente().getClienteId() != null) {
            Cliente cliente = clienteService.buscarPorId(reserva.getCliente().getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            reserva.setCliente(cliente);
        }

        // ⚡ Hidratar Paquete antes de guardar
        if (reserva.getPaquete() != null && reserva.getPaquete().getPaqueteId() != null) {
            Paquete paquete = paqueteService.buscarPorId(reserva.getPaquete().getPaqueteId())
                    .orElseThrow(() -> new IllegalArgumentException("Paquete no encontrado"));
            reserva.setPaquete(paquete);
        }

        return reservaRepository.save(reserva);
    }

    @Override
    public void eliminarPorId(Integer id) {
        reservaRepository.deleteById(id);
    }
}