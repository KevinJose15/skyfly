package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Reservas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservaId;

    @ManyToOne
    @JoinColumn(name = "clienteId", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "paqueteId", nullable = false)
    private Paquete paquete;

    private LocalDateTime fechaReserva = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    // ----------------- Constructores, getters y setters -----------------

    public Reservas() {}
    

    public Integer getReservaId() {
        return reservaId;
    }

    public void setReservaId(Integer reservaId) {
        this.reservaId = reservaId;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Paquete getPaquete() {
        return paquete;
    }

    public void setPaquete(Paquete paquete) {
        this.paquete = paquete;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }
    @PrePersist
public void prePersist() {
    if (fechaReserva == null) {
        fechaReserva = LocalDateTime.now();
    }
    if (estado == null) {
        estado = EstadoReserva.PENDIENTE;
    }
}

}
