package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

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

     @Column(name = "fechaPago")
    private LocalDateTime fechaPago = LocalDateTime.now();
    @Transient
    private String numeroTarjeta; // temporal, para validar Luhn

    @Column(length = 4)
    private String ultimos4Tarjeta;

    @Transient
    private BigDecimal iva;   // 13% del monto (no se persiste)
    @Transient
    private BigDecimal total; // monto + iva   (no se persiste)


    @Enumerated(EnumType.STRING)
    private EstadoReserva estadoPago = EstadoReserva.PENDIENTE;

    private String codigoAutorizacion;

    @ManyToOne
    @JoinColumn(name = "reservaId", referencedColumnName = "reservaId", insertable = false, updatable = false)
    private Reservas reserva;

    @ManyToOne
    @JoinColumn(name = "metodoPagoId", referencedColumnName = "metodoPagoId", insertable = false, updatable = false)
    private MetodoPago metodoPago;

    

    public Pago() {
    }

  public BigDecimal getIva() {
        if (iva == null && monto != null) {
            iva = monto.multiply(new BigDecimal("0.13"))
                       .setScale(2, RoundingMode.HALF_UP);
        }
        return iva;
    }
    public void setIva(BigDecimal iva) { this.iva = iva; }

    public BigDecimal getTotal() {
        if (total == null) {
            BigDecimal base = (monto != null) ? monto : BigDecimal.ZERO;
            BigDecimal ivaCalc = (getIva() != null) ? getIva() : BigDecimal.ZERO;
            total = base.add(ivaCalc).setScale(2, RoundingMode.HALF_UP);
        }
        return total;
    }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Pago(Integer pagoId, @NotNull(message = "La reserva es requerida") Integer reservaId,
            @NotNull(message = "El monto es requerido") BigDecimal monto,
            @NotNull(message = "El método de pago es requerido") Integer metodoPagoId, LocalDateTime fechaPago,
            String numeroTarjeta, String ultimos4Tarjeta, EstadoReserva estadoPago, String codigoAutorizacion,
            Reservas reserva, MetodoPago metodoPago) {
        this.pagoId = pagoId;
        this.reservaId = reservaId;
        this.monto = monto;
        this.metodoPagoId = metodoPagoId;
        this.fechaPago = fechaPago;
        this.numeroTarjeta = numeroTarjeta;
        this.ultimos4Tarjeta = ultimos4Tarjeta;
        this.estadoPago = estadoPago;
        this.codigoAutorizacion = codigoAutorizacion;
        this.reserva = reserva;
        this.metodoPago = metodoPago;
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



    public LocalDateTime getFechaPago() {
        return fechaPago;
    }



    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }



    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }



    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
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