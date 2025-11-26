package esfe.skyfly.Servicios.Implementaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Repositorios.IFacturaRepository;
import esfe.skyfly.Servicios.Interfaces.IFacturaService;
import esfe.skyfly.Utilidades.PdfFacturaUtil;

@Service
public class FacturaService implements IFacturaService {

    private final IFacturaRepository facturaRepository;
    private final PdfFacturaUtil pdfFacturaUtil;

    // IVA (por ejemplo 13% = 0.13). Si no está en properties, usa 0.13.
    @Value("${skyfly.iva:0.13}")
    private BigDecimal iva;

    public FacturaService(IFacturaRepository facturaRepository, PdfFacturaUtil pdfFacturaUtil) {
        this.facturaRepository = facturaRepository;
        this.pdfFacturaUtil = pdfFacturaUtil;
    }

 @Override
@Transactional
public Factura crearDesdePago(Pago pago) {

    if (pago == null) throw new IllegalArgumentException("Pago nulo.");
    if (pago.getReserva() == null) throw new IllegalArgumentException("El pago no tiene reserva.");
    if (pago.getReserva().getPaquete() == null) throw new IllegalArgumentException("La reserva no tiene paquete.");
    if (pago.getReserva().getPaquete().getPrecio() == null) throw new IllegalArgumentException("El paquete no tiene precio.");

    // ✅ MONTO BASE
    BigDecimal base = pago.getReserva().getPaquete().getPrecio();

    // ✅ IVA
    BigDecimal impuestos = base.multiply(iva)
            .setScale(2, RoundingMode.HALF_UP);

    Factura f = new Factura();
    f.setFechaEmision(LocalDate.now());
    f.setReserva(pago.getReserva());

    // ✅ GUARDAMOS CORRECTAMENTE
    f.setMontoTotal(base);        // Base SIN IVA
    f.setImpuestos(impuestos);    // IVA

    // ✅ NO GUARDES EL TOTAL (se calcula solo en getTotalAPagar())
    return facturaRepository.save(f);
}

    @Override
    public Optional<Factura> buscarPorId(Integer id) {
        return facturaRepository.findById(id);
    }

    @Override
    public List<Factura> listar() {
        return facturaRepository.findAll();
    }

    @Override
    public List<Factura> listarPorReserva(Reservas reserva) {
        return facturaRepository.findByReserva(reserva);
    }

    @Override
    public void eliminar(Integer id) {
        facturaRepository.deleteById(id);
    }

    @Override
    public byte[] generarPdf(Factura factura) {
        return pdfFacturaUtil.generarPdfFactura(factura);
    }

@Override
public Optional<Factura> buscarDetallePorId(Integer id) {
    // ahora este findById está anotado con @EntityGraph
    return facturaRepository.findById(id);

}
 @Override
    public Optional<Factura> buscarPorReservaId(Integer reservaId) {
        return facturaRepository.findByReserva_ReservaId(reservaId);
    }
}