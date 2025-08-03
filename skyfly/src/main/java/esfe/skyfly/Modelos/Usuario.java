package esfe.skyfly.Modelos;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotBlank(message = "La contraseña es requerida")
    private String passwordHash;

    @NotBlank(message = "El correo es requerido")
    @Email(message = "Debe proporcionar un correo válido")
    private String email;

    private int status;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(Integer id, @NotBlank(message = "El nombre es requerido") String name,
            @NotBlank(message = "La contraseña es requerida") String passwordHash,
            @NotBlank(message = "El correo es requerido") @Email(message = "Debe proporcionar un correo válido") String email,
            int status, Rol rol) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
        this.email = email;
        this.status = status;
        this.rol = rol;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
        public String getStrEstatus() {
        String str = "";
        switch (status) {
            case 1:
                str = "ACTIVO";
                break;
            case 0:
                str = "INACTIVO";
                break;
            default:
                str = "DESCONOCIDO";
        }
        return str;
    }
}
