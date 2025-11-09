package esfe.skyfly.ai.dto;
import lombok.*;

import java.util.List;

@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PreferenciasViaje {
  private String origenIATA;       // "SAL"
  private String fechaViaje;       // "2026-01"
  private Integer duracionDias;    // 5..10
  private Integer presupuestoUSD;  // 800..1500
  private String clima;            // "templado","calor","frío"
  private List<String> tipo;       // ["playa","cultura","naturaleza"]
  private String companeros;       // "solo","pareja","familia","amigos"
}
