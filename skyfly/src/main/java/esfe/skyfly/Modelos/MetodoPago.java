package esfe.skyfly.Modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer metodoPagoId;

    @NotBlank(message = "El nombre del método es requerido")
    @Column(unique = true)
    private String nombreMetodo;

    
    public MetodoPago() {}

    
    public MetodoPago(Integer metodoPagoId, String nombreMetodo) {
        this.metodoPagoId = metodoPagoId;
        this.nombreMetodo = nombreMetodo;
    }


    public Integer getMetodoPagoId() {
        return metodoPagoId;
    }

    public void setMetodoPagoId(Integer metodoPagoId) {
        this.metodoPagoId = metodoPagoId;
    }

    public String getNombreMetodo() {
        return nombreMetodo;
    }

    public void setNombreMetodo(String nombreMetodo) {
        this.nombreMetodo = nombreMetodo;
    }

    @Override
    public String toString() {
        return nombreMetodo;
    }
}
