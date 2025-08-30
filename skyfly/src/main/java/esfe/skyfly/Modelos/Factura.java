package esfe.skyfly.Modelos;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "factura")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_factura") // <-- mantiene el nombre en la BD
    private Integer idFactura;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "impuestos", nullable = false, precision = 10, scale = 2)
    private BigDecimal impuestos;

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reservas reserva;

    @Column(name="totalapagar", insertable=false, updatable=false)
private BigDecimal totalApagar;

    // Campo calculado en Java
    @Transient
    public BigDecimal getTotalAPagar() {
        return montoTotal != null && impuestos != null
                ? montoTotal.add(impuestos)
                : BigDecimal.ZERO;
    }

    // ===== Getters y Setters =====
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

    public Reservas getReserva() {
        return reserva;
    }

    public void setReserva(Reservas reserva) {
        this.reserva = reserva;
    }
}