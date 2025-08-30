package esfe.skyfly.Servicios.Implementaciones;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Repositorios.IPagoRepository;
import esfe.skyfly.Repositorios.IReservaRepository;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Servicios.Interfaces.IFacturaService;
import esfe.skyfly.Servicios.Interfaces.IPagoService;

@Service
public class PagoServiceImpl implements IPagoService {

    @Autowired private IPagoRepository pagoRepository;
    @Autowired private IReservaRepository reservasRepository;
    @Autowired private CodigoConfirmacionService CodigoConfirmacionServiceImpl;
    @Autowired private IFacturaService facturaService;

    @Override
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }

    /**
     * Crear/editar sin generar factura (la factura se crea en aprobarPagoConCodigo).
     * Si por algún caso el pago ya viene APROBADA, sí crea factura (modo admin/manual).
     */
    @Override
    @Transactional
    public Pago crearOeditar(Pago pago) {
        Pago saved = pagoRepository.save(pago);
        if (saved.getEstadoPago() == EstadoReserva.CONFIRMADA) {
            // Evita duplicados si ya existe factura de esa reserva
            if (facturaService.listarPorReserva(saved.getReserva()).isEmpty()) {
                facturaService.crearDesdePago(saved);
            }
        }
        return saved;
    }

    @Override
    public void eliminarPorId(Integer id) {
        pagoRepository.deleteById(id);
    }

    @Override
    public Optional<Pago> buscarUltimoPagoPendientePorCliente(String email) {
        return pagoRepository.findFirstByReserva_Cliente_Usuario_EmailAndEstadoPagoOrderByFechaPagoDesc(
                email, EstadoReserva.PENDIENTE);
    }

    /**
     * Flujo oficial:
     * 1) Validar código con el email del usuario dueño de la reserva
     * 2) Marcar Pago y Reserva como APROBADA
     * 3) Guardar cambios (transacción)
     * 4) Crear Factura si aún no existe para esa reserva
     */
    @Transactional
    public Pago aprobarPagoConCodigo(Integer idPago, String codigoIngresado) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        Reservas reserva = pago.getReserva();
        if (reserva == null) throw new IllegalStateException("El pago no tiene reserva asociada.");
        if (reserva.getCliente() == null || reserva.getCliente().getUsuario() == null) {
            throw new IllegalStateException("La reserva no tiene un usuario asociado.");
        }

        String email = reserva.getCliente().getUsuario().getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("El usuario asociado a la reserva no tiene email.");
        }

        // 1) Validar código con tu servicio real
        boolean codigoOk = CodigoConfirmacionServiceImpl.validarCodigo(email, codigoIngresado);
        if (!codigoOk) {
            throw new IllegalArgumentException("Código de confirmación inválido.");
        }

        // 2) Estados -> APROBADA (usando tu enum EstadoReserva)
        pago.setEstadoPago(EstadoReserva.CONFIRMADA);
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        // 3) Persistir ambos bajo la misma transacción
        reservasRepository.save(reserva);
        Pago saved = pagoRepository.save(pago);

        // 4) Crear factura SOLO aquí y SOLO si no existe una para la reserva
        if (facturaService.listarPorReserva(reserva).isEmpty()) {
            facturaService.crearDesdePago(saved);
        }

        return saved;
    }
}