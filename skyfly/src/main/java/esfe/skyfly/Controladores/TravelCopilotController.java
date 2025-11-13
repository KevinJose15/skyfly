package esfe.skyfly.Controladores;

import esfe.skyfly.ai.TravelCopilotService;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import esfe.skyfly.ai.dto.RecomendacionDTO;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TravelCopilotController {

  private final TravelCopilotService copilot;

  public TravelCopilotController(TravelCopilotService copilot) {
    this.copilot = copilot;
  }

  // Texto plano (fallback del frontend) — misma ruta SIN conflicto
  @PostMapping(
      path = "/api/ai/destinos",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  public ResponseEntity<String> recomendarTexto(@RequestBody PreferenciasViaje prefs) {
    return ResponseEntity.ok(copilot.recomendar(prefs));
  }

  // JSON estructurado para las tarjetas
  @PostMapping(
      path = "/api/ai/destinos/json",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Map<String, Object>> recomendarJson(@RequestBody PreferenciasViaje prefs) {
    List<RecomendacionDTO> items = copilot.recomendarEstructurado(prefs);
    return ResponseEntity.ok(Map.of("items", items));
  }
}
