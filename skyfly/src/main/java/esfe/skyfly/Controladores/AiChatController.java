package esfe.skyfly.Controladores;

import esfe.skyfly.ai.TravelCopilotService;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

  private static final Logger log = LoggerFactory.getLogger(AiChatController.class);
  private final TravelCopilotService copilot;

  public AiChatController(TravelCopilotService copilot) {
    this.copilot = copilot;
  }

  // Health-check rápido: http://localhost:8080/api/ai/ping
  @GetMapping("/ping")
  public Map<String, Object> ping() {
    return Map.of("ok", true, "ts", Instant.now().toString());
  }

  @PostMapping(
      value = "/destinos",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<RespuestaChat> destinos(@RequestBody PreferenciasViaje prefs) {
    try {
      String text = copilot.recomendar(prefs);
      return ResponseEntity.ok(new RespuestaChat(text));
    } catch (Exception e) {
      log.error("AI endpoint error, devolviendo sólo BD", e);
      String text = copilot.recomendarSoloBD(prefs);
      return ResponseEntity.ok(new RespuestaChat(text));
    }
  }

  public static class RespuestaChat {
    private final String text;
    public RespuestaChat(String text){ this.text = text; }
    public String getText(){ return text; }
  }
}
