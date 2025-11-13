package esfe.skyfly.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public class RecomendacionDTO {
  private Integer id;
  private String nombre;
  private String pais;
  private BigDecimal precio;
  private String descripcion;
  private List<String> tags;
  private String imagen;     // <-- URL de imagen

  public RecomendacionDTO() {}

  public RecomendacionDTO(
      Integer id, String nombre, String pais, BigDecimal precio,
      String descripcion, List<String> tags, String imagen
  ) {
    this.id = id;
    this.nombre = nombre;
    this.pais = pais;
    this.precio = precio;
    this.descripcion = descripcion;
    this.tags = tags;
    this.imagen = imagen;
  }

  public Integer getId() { return id; }
  public String getNombre() { return nombre; }
  public String getPais() { return pais; }
  public BigDecimal getPrecio() { return precio; }
  public String getDescripcion() { return descripcion; }
  public List<String> getTags() { return tags; }
  public String getImagen() { return imagen; }

  public void setId(Integer id) { this.id = id; }
  public void setNombre(String nombre) { this.nombre = nombre; }
  public void setPais(String pais) { this.pais = pais; }
  public void setPrecio(BigDecimal precio) { this.precio = precio; }
  public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
  public void setTags(List<String> tags) { this.tags = tags; }
  public void setImagen(String imagen) { this.imagen = imagen; }
}
