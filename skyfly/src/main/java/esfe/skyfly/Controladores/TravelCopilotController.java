package esfe.skyfly.Controladores;

import esfe.skyfly.ai.TravelCopilotService;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import esfe.skyfly.ai.dto.RecomendacionDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/destinos")
public class TravelCopilotController {

  private final TravelCopilotService copilot;

  public TravelCopilotController(TravelCopilotService copilot) {
    this.copilot = copilot;
  }

  /** Fallback en TEXTO PLANO (ruta dedicada /text) */
  @PostMapping(
      path = "/text",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public ResponseEntity<String> recomendarTexto(@RequestBody PreferenciasViaje prefs) {
    return ResponseEntity.ok(copilot.recomendar(prefs));
  }

  /** Respuesta ESTRUCTURADA en JSON */
  @PostMapping(
      path = "/json",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Map<String, Object>> recomendarJson(@RequestBody PreferenciasViaje prefs) {
    List<RecomendacionDTO> items = copilot.recomendarEstructurado(prefs);
    return ResponseEntity.ok(Map.of("items", items));
  }
}
