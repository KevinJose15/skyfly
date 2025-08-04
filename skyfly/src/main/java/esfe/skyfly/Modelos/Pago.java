package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pagoId;

    @NotNull(message = "La reserva es requerida")
    private Integer reservaId;

    @NotNull(message = "El monto es requerido")
    private BigDecimal monto;

    @NotNull(message = "El método de pago es requerido")
    private Integer metodoPagoId;

    private LocalDate fechaPago = LocalDate.now();

    @Column(length = 4)
    private String ultimos4Tarjeta;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estadoPago = EstadoReserva.PENDIENTE;

    private String codigoAutorizacion;

    @ManyToOne
    @JoinColumn(name = "reservaId", referencedColumnName = "reservaId", insertable = false, updatable = false)
    private Reservas reserva;

    @ManyToOne
    @JoinColumn(name = "metodoPagoId", referencedColumnName = "metodoPagoId", insertable = false, updatable = false)
    private MetodoPago metodoPago;

    public Pago() {}

    public Pago(Integer pagoId, Integer reservaId, BigDecimal monto, Integer metodoPagoId,
                LocalDate fechaPago, String ultimos4Tarjeta, EstadoReserva estadoPago, String codigoAutorizacion) {
        this.pagoId = pagoId;
        this.reservaId = reservaId;
        this.monto = monto;
        this.metodoPagoId = metodoPagoId;
        this.fechaPago = fechaPago;
        this.ultimos4Tarjeta = ultimos4Tarjeta;
        this.estadoPago = estadoPago;
        this.codigoAutorizacion = codigoAutorizacion;
    }

    public Integer getPagoId() {
        return pagoId;
    }

    public void setPagoId(Integer pagoId) {
        this.pagoId = pagoId;
    }

    public Integer getReservaId() {
        return reservaId;
    }

    public void setReservaId(Integer reservaId) {
        this.reservaId = reservaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Integer getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Integer metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getUltimos4Tarjeta() {
        return ultimos4Tarjeta;
    }

    public void setUltimos4Tarjeta(String ultimos4Tarjeta) {
        this.ultimos4Tarjeta = ultimos4Tarjeta;
    }

    public EstadoReserva getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoReserva estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getCodigoAutorizacion() {
        return codigoAutorizacion;
    }

    public void setCodigoAutorizacion(String codigoAutorizacion) {
        this.codigoAutorizacion = codigoAutorizacion;
    }

    public Reservas getReserva() {
        return reserva;
    }

    public void setReserva(Reservas reserva) {
        this.reserva = reserva;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}