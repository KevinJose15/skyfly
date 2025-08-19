package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Entity
public class Cliente {
  @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clienteId")
    private Integer clienteId;

    @NotBlank(message = "El número de teléfono es requerido")
    @Size(max = 20, message = "El número no puede exceder los 20 caracteres")
    private String telefono;

    @NotBlank(message = "La dirección es requerida")
    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;

    @OneToOne
    @JoinColumn(name = "usuarioId", referencedColumnName = "id", nullable = false, unique = true)
    private Usuario usuario;

    public Cliente() {}

    public Cliente(Integer clienteId, String telefono, String direccion, Usuario usuario) {
        this.clienteId = clienteId;
        this.telefono = telefono;
        this.direccion = direccion;
        this.usuario = usuario;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
