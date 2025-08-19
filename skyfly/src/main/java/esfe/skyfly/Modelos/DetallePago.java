package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "Factura")
public class DetallePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFactura;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "El monto total es obligatorio")
    @PositiveOrZero(message = "El monto total debe ser cero o positivo")
    private BigDecimal montoTotal;

    @PositiveOrZero(message = "Los impuestos deben ser cero o positivos")
    private BigDecimal impuestos = BigDecimal.ZERO;

    @PositiveOrZero(message = "El descuento aplicado debe ser cero o positivo")
    private BigDecimal descuentoAplicado = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "reservaId", referencedColumnName = "reservaId", unique = true, nullable = false)
    private Reservas reserva;


    public DetallePago() {
    }

    public DetallePago(Integer idFactura, LocalDate fechaEmision,
                       BigDecimal montoTotal, BigDecimal impuestos, BigDecimal descuentoAplicado,
                       Reservas reserva) {
        this.idFactura = idFactura;
        this.fechaEmision = fechaEmision;
        this.montoTotal = montoTotal;
        this.impuestos = impuestos;
        this.descuentoAplicado = descuentoAplicado;
        this.reserva = reserva;
    }


    public Integer getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(BigDecimal impuestos) {
        this.impuestos = impuestos;
    }

    public BigDecimal getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public void setDescuentoAplicado(BigDecimal descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public Reservas getReserva() {
        return reserva;
    }

    public void setReserva(Reservas reserva) {
        this.reserva = reserva;
    }
}
