package esfe.skyfly.ai;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Repositorios.IDestinoRepository;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelCopilotService {

  private static final Logger log = LoggerFactory.getLogger(TravelCopilotService.class);

  private final GeminiChatService gemini;
  private final IDestinoRepository destinoRepo;

  public TravelCopilotService(GeminiChatService gemini, IDestinoRepository destinoRepo) {
    this.gemini = gemini;
    this.destinoRepo = destinoRepo;
  }

  public String recomendar(PreferenciasViaje prefs) {
    try {
      var candidatos = obtenerCandidatos(prefs);
      try {
        // Construcción de prompt + llamada al modelo
        String prompt = PromptBuilder.build(prefs, candidatos);
        return gemini.generatePlainText(prompt);
      } catch (Exception ia) {
        log.warn("IA/Prompt falló, usando fallback local", ia);
        return buildFallbackText(prefs, candidatos);
      }
    } catch (Exception any) {
      log.error("Error recuperando candidatos, fallback total", any);
      // último recurso: intenta sin filtros y arma fallback básico
      try {
        var candidatos = obtenerCandidatosSoloListado();
        return buildFallbackText(prefs, candidatos);
      } catch (Exception still) {
        log.error("Fallback total también falló", still);
        return "No pude generar recomendaciones en este momento. Intenta más tarde.";
      }
    }
  }

  /** Úsalo desde el controller si quieres forzar sólo BD (sin IA). */
  public String recomendarSoloBD(PreferenciasViaje prefs) {
    var candidatos = obtenerCandidatos(prefs);
    return buildFallbackText(prefs, candidatos);
  }

  /* ------------------ helpers ------------------ */

  private List<Map<String,Object>> obtenerCandidatos(PreferenciasViaje prefs) {
    Pageable pageReq = PageRequest.of(0, 12, Sort.by("nombre").ascending());
    Page<Destino> page;

    boolean tieneFiltroTipo = prefs.getTipo() != null && !prefs.getTipo().isEmpty();
    boolean tieneFinder = hasFinderMethod();

    if (tieneFiltroTipo && tieneFinder) {
      String kw = String.join(" ", prefs.getTipo());
      page = destinoRepo
          .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(kw, kw, pageReq);
      if (page.isEmpty()) page = destinoRepo.findAll(pageReq);
    } else {
      page = destinoRepo.findAll(pageReq);
    }

    return page.getContent().stream().map(this::toCandidate).collect(Collectors.toList());
  }

  private List<Map<String,Object>> obtenerCandidatosSoloListado() {
    Pageable pageReq = PageRequest.of(0, 12, Sort.by("nombre").ascending());
    var page = destinoRepo.findAll(pageReq);
    return page.getContent().stream().map(this::toCandidate).collect(Collectors.toList());
  }

  private boolean hasFinderMethod() {
    try {
      IDestinoRepository.class.getMethod(
          "findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase",
          String.class, String.class, org.springframework.data.domain.Pageable.class);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  private Map<String,Object> toCandidate(Destino d) {
    Integer id = null;
    try { id = (Integer) d.getClass().getMethod("getDestinoId").invoke(d); }
    catch (Exception ignore) { try { id = (Integer) d.getClass().getMethod("getIdDestino").invoke(d); }
    catch (Exception ignore2) { try { id = (Integer) d.getClass().getMethod("getId").invoke(d); } catch (Exception ignore3) {} } }

    String nombre = safeInvoke(d, "getNombre");
    String pais   = safeInvoke(d, "getPais");
    String desc   = safeInvoke(d, "getDescripcion");

    Map<String,Object> m = new LinkedHashMap<>();
    m.put("id", id);
    m.put("nombre", nombre);
    m.put("pais", pais);
    m.put("costoMedioUSD", estimarCosto(desc, pais));     // cambia a costo real si conectas paqueteRepo
    m.put("tags", inferirTags(nombre, desc));
    return m;
  }

  private static String safeInvoke(Destino d, String getter) {
    try { Object v = d.getClass().getMethod(getter).invoke(d); return v!=null ? String.valueOf(v) : null; }
    catch (Exception e) { return null; }
  }

  private static int estimarCosto(String desc, String pais) {
    String txt = ((desc==null?"":desc)+" "+(pais==null?"":pais)).toLowerCase();
    int base = 1000;
    if (txt.contains("méxico") || txt.contains("mexico")) base -= 100;
    if (txt.contains("japón")  || txt.contains("japon"))  base += 400;
    if (txt.contains("perú")   || txt.contains("peru"))   base += 150;
    return Math.max(400, base);
  }

  private static List<String> inferirTags(String nombre, String desc) {
    String txt = ((nombre==null?"":nombre)+" "+(desc==null?"":desc)).toLowerCase();
    List<String> tags = new ArrayList<>();
    if (txt.contains("playa") || txt.contains("isla") || txt.contains("lago")) tags.add("playa");
    if (txt.contains("ruina") || txt.contains("hist") || txt.contains("imperio") || txt.contains("arqueolog")) tags.add("cultura");
    if (txt.contains("monte") || txt.contains("volcán") || txt.contains("volcan") || txt.contains("natur")) tags.add("naturaleza");
    if (txt.contains("nocturn")) tags.add("vidaNocturna");
    if (txt.contains("gastron")) tags.add("gastronomía");
    return tags.isEmpty() ? List.of("turismo") : tags;
  }

  private String buildFallbackText(PreferenciasViaje prefs, List<Map<String,Object>> cand) {
    List<String> wish = Optional.ofNullable(prefs.getTipo()).orElse(List.of()).stream()
      .map(String::toLowerCase).toList();

    Comparator<Map<String,Object>> cmp = Comparator
      .comparingInt((Map<String,Object> m) -> -overlap(wish, (List<String>) m.getOrDefault("tags", List.of())))
      .thenComparingInt(m -> ((Number)Optional.ofNullable(m.get("costoMedioUSD")).orElse(999999)).intValue());

    List<Map<String,Object>> top = cand.stream().sorted(cmp).limit(3).toList();

    StringBuilder sb = new StringBuilder("**(Fallback sin IA)** Recomendaciones según tu BD:\n");
    int i=1;
    for (var m : top) {
      String nombre = String.valueOf(m.getOrDefault("nombre","(sin nombre)"));
      String pais   = String.valueOf(m.getOrDefault("pais",""));
      Object costo  = m.get("costoMedioUSD");
      sb.append(i++).append(") ").append(nombre).append(" — ").append(pais);
      if (costo != null) sb.append(" — costo ~USD ").append(costo);
      sb.append("\n   • Coincidencia: ").append(String.valueOf(m.getOrDefault("tags", List.of()))).append("\n");
    }
    return sb.toString();
  }

  private static int overlap(List<String> a, List<String> b) {
    if (a==null || b==null) return 0;
    var set = new HashSet<>(b.stream().map(String::toLowerCase).toList());
    int c=0; for (var x: a) if (set.contains(x.toLowerCase())) c++; return c;
  }
}
