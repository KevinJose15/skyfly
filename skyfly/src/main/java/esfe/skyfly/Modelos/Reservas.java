package esfe.skyfly.Modelos;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Reservas {
        @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservaId;

    @NotNull(message = "El cliente es requerido")
    private Integer clienteId;

    @NotNull(message = "El paquete es requerido")
    private Integer paqueteId;

    private LocalDateTime  fechaReserva = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "clienteId", referencedColumnName = "clienteId", insertable = false, updatable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "paqueteId", referencedColumnName = "paqueteId", insertable = false, updatable = false)
    private Paquete paquete;

    public Reservas() {
    }

    public Reservas(Integer reservaId, @NotNull(message = "El cliente es requerido") Integer clienteId,
            @NotNull(message = "El paquete es requerido") Integer paqueteId, LocalDateTime fechaReserva,
            EstadoReserva estado, Cliente cliente, Paquete paquete) {
        this.reservaId = reservaId;
        this.clienteId = clienteId;
        this.paqueteId = paqueteId;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.cliente = cliente;
        this.paquete = paquete;
    }

    public Integer getReservaId() {
        return reservaId;
    }

    public void setReservaId(Integer reservaId) {
        this.reservaId = reservaId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Integer paqueteId) {
        this.paqueteId = paqueteId;
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

    
}
