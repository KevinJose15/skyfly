package esfe.skyfly.Servicios.Interfaces;

import java.util.List;
import java.util.Optional;

import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;

public interface IFacturaService {
    Factura crearDesdePago(Pago pago);
    Optional<Factura> buscarPorId(Integer id);
    List<Factura> listar();
    List<Factura> listarPorReserva(Reservas reserva);
    void eliminar(Integer id);
    byte[] generarPdf(Factura factura);
    Optional<Factura> buscarDetallePorId(Integer id);
}