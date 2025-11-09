package esfe.skyfly.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import esfe.skyfly.ai.dto.PreferenciasViaje;

import java.util.List;
import java.util.Map;

public class PromptBuilder {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  // ⬇️ Firma que esperamos desde TravelCopilotService
  public static String build(PreferenciasViaje prefs, List<Map<String, Object>> candidatos) {
    try {
      String system = """
      Eres el Travel Copilot de SkyFly.
      Recomienda 3 destinos de la lista de candidatos, con 2-3 razones y costo ~USD según 'costoMedioUSD'.
      Si faltan datos críticos, haz UNA repregunta breve. Responde en texto plano.
      Formato:
      1) <Destino> — <País> — costo ~USD <costoMedioUSD>
         • razón 1
         • razón 2
      2) ...
      3) ...
      """;

      String prefsJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(prefs);
      String candJson  = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(candidatos);

      return system + "\n---\nPreferencias:\n" + prefsJson + "\n\nCandidatos:\n" + candJson;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
