package esfe.skyfly.ai.dto;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DestinoCandidato {
  private Long id;
  private String nombre;
  private String pais;
  private List<String> mejorEpoca;   // ["dic","ene","feb"]
  private Integer costoMedioUSD;     // media histórica
  private List<String> tags;         // ["playa","gastronomía","vidaNocturna"]
  private double scorePopularidad;   // 0..5
}
