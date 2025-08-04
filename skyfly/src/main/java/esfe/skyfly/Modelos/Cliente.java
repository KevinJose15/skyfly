package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Cliente {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer clienteId;

    @Column(unique = true)
    private Integer userId;

    @NotBlank(message = "El número de teléfono es requerido")
    @Size(max = 20, message = "El número no puede exceder los 20 caracteres")
    private String telefono;

    @NotBlank(message = "La dirección es requerida")
    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;

    @OneToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false)
    private Usuario usuario;
    
    public Cliente() {} 

    public Cliente(Integer clienteId, Integer userId, String telefono, String direccion) {
        this.clienteId = clienteId;
        this.userId = userId;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    public Integer getClienteId() {
        return clienteId;
    }   
    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
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

}
