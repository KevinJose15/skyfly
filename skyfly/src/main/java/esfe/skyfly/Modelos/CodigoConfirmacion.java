package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "CodigoConfirmacion")
public class CodigoConfirmacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCodigo;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Debe ser un email válido")
    private String email;

    @NotBlank(message = "El código es requerido")
    @Column(length = 10)
    private String codigo;

    private LocalDateTime fechaGeneracion = LocalDateTime.now();
@Column(columnDefinition = "TINYINT(1)")
    private boolean usado = false;

    public CodigoConfirmacion() {}

    public CodigoConfirmacion(Integer idCodigo, String email, String codigo,
                              LocalDateTime fechaGeneracion, boolean usado) {
        this.idCodigo = idCodigo;
        this.email = email;
        this.codigo = codigo;
        this.fechaGeneracion = fechaGeneracion;
        this.usado = usado;
    }

    public Integer getIdCodigo() {
        return idCodigo;
    }

    public void setIdCodigo(Integer idCodigo) {
        this.idCodigo = idCodigo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}
