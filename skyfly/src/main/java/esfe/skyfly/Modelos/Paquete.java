package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "paquete")

public class Paquete {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paquete_Id")
    private Integer paqueteId;

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "La descripción es requerida")
    private String descripcion;

    @Positive(message = "El precio debe ser positivo")
    private BigDecimal precio;

    @Positive(message = "La duración debe ser positiva")
    @Column(name = "duracion_dias")
    private int duracionDias;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fechaInicio")
    private LocalDate fechaInicio;

    @Column(name = "fechaFin")
    private LocalDate fechaFin;

    // Relación con Destino
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destino_id", nullable = false) // FK en la tabla Paquete
    private Destino destino;

    public Paquete() {
    }

    public Paquete(Integer paqueteId, String nombre, String descripcion, BigDecimal precio,
                   int duracionDias, LocalDate fechaInicio, LocalDate fechaFin, Destino destino) {
        this.paqueteId = paqueteId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracionDias = duracionDias;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.destino = destino;
    }

    public Integer getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Integer paqueteId) {
        this.paqueteId = paqueteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

      // Calcular fechaFin automáticamente si no existe
    @PrePersist
    @PreUpdate
    public void calcularFechaFin() {
        if (fechaInicio != null && duracionDias > 0) {
            this.fechaFin = fechaInicio.plusDays(duracionDias);
        }
    }
} 