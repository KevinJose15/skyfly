package esfe.skyfly.ai;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Repositorios.IDestinoRepository;
import esfe.skyfly.Repositorios.IPaqueteRepository;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import esfe.skyfly.ai.dto.RecomendacionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TravelCopilotService {

  private static final Logger log = LoggerFactory.getLogger(TravelCopilotService.class);

  private final GeminiChatService gemini;
  private final IDestinoRepository destinoRepo;
  private final IPaqueteRepository paqueteRepo;

  public TravelCopilotService(
      GeminiChatService gemini,
      IDestinoRepository destinoRepo,
      IPaqueteRepository paqueteRepo
  ) {
    this.gemini = gemini;
    this.destinoRepo = destinoRepo;
    this.paqueteRepo = paqueteRepo;
  }

  /* ===================== PÚBLICOS ===================== */

  public String recomendar(PreferenciasViaje prefs) {
    try {
      var candidatos = obtenerCandidatos(prefs);
      try {
        String prompt = PromptBuilder.build(
            prefs,
            candidatos.stream().map(Cand::toMap).toList()
        );
        return gemini.generatePlainText(prompt);
      } catch (Exception ia) {
        log.warn("IA/Prompt falló, usando fallback local", ia);
        return buildFallbackText(prefs, candidatos);
      }
    } catch (Exception any) {
      log.error("Error recuperando candidatos, fallback total", any);
      try {
        var candidatos = obtenerCandidatosSoloListado();
        return buildFallbackText(prefs, candidatos);
      } catch (Exception still) {
        log.error("Fallback total también falló", still);
        return "No pude generar recomendaciones en este momento. Intenta más tarde.";
      }
    }
  }

  public String recomendarSoloBD(PreferenciasViaje prefs) {
    var candidatos = obtenerCandidatos(prefs);
    return buildFallbackText(prefs, candidatos);
  }

  /** Devuelve lista estructurada para el endpoint JSON. */
  public List<RecomendacionDTO> recomendarEstructurado(PreferenciasViaje prefs) {
    // Usa el pipeline actual (ya filtra por tipo y presupuesto)
    List<Cand> cand = obtenerCandidatos(prefs);
    String descBase = buildDescripcion(prefs);

    return cand.stream().map(c -> {
      RecomendacionDTO dto = new RecomendacionDTO();
      dto.setId(c.id);
      dto.setNombre(c.nombre);
      dto.setPais(c.pais);
      dto.setPrecio(c.precio);
      dto.setDescripcion(descBase);
      dto.setTags(c.tags);
      dto.setImagen(c.imagen);  // <-- URL real desde tu BD
      return dto;
    }).toList();
  }

  /* ===================== Modelo interno ===================== */

  private static final class Cand {
    final Integer id;
    final String nombre;
    final String pais;
    final BigDecimal precio;
    final List<String> tags;
    final String imagen;  // <-- ahora se llama imagen

    Cand(Integer id, String nombre, String pais, BigDecimal precio, List<String> tags, String imagen) {
      this.id = id;
      this.nombre = nombre;
      this.pais = pais;
      this.precio = (precio != null ? precio : BigDecimal.ZERO);
      this.tags = (tags != null ? List.copyOf(tags) : List.of());
      this.imagen = (imagen != null && !imagen.isBlank()) ? imagen.trim() : null;
    }
    Map<String,Object> toMap() {
      Map<String,Object> m = new LinkedHashMap<>();
      m.put("id", id);
      m.put("nombre", nombre);
      m.put("pais", pais);
      m.put("costoMedioUSD", precio);
      m.put("tags", tags);
      m.put("imagen", imagen); // útil si en algún momento pasas candidatos a IA
      return m;
    }
  }

  /* ===================== Construcción de candidatos ===================== */

  private List<Cand> obtenerCandidatos(PreferenciasViaje prefs) {
    Pageable pageReq = PageRequest.of(0, 200, Sort.by("nombre").ascending());
    Page<Destino> page;

    boolean tieneFinder = hasFinderMethod();
    boolean tieneFiltroTipo = prefs.getTipo() != null && !prefs.getTipo().isEmpty();

    if (tieneFiltroTipo && tieneFinder) {
      String kw = String.join(" ", prefs.getTipo());
      page = destinoRepo
          .findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(kw, kw, pageReq);
      if (page.isEmpty()) page = destinoRepo.findAll(pageReq);
    } else {
      page = destinoRepo.findAll(pageReq);
    }

    List<Cand> base = page.getContent().stream().map(this::toCandidate).collect(Collectors.toList());

    // Presupuesto: acepta DTO nuevo (min/max) o viejo (presupuestoUSD único)
    final BigDecimal min = readBudget(prefs, true);
    final BigDecimal max = readBudget(prefs, false);

    final List<String> wish = Optional.ofNullable(prefs.getTipo()).orElse(List.of())
        .stream().filter(Objects::nonNull)
        .map(s -> s.toLowerCase(Locale.ROOT))
        .map(TravelCopilotService::norm)
        .toList();

    // Filtro REAL (tags + presupuesto)
    List<Cand> filtrados = base.stream()
        .filter(c -> min == null || c.precio.compareTo(min) >= 0)
        .filter(c -> max == null || c.precio.compareTo(max) <= 0)
        .filter(c -> wish.isEmpty() || hasAnyTag(c.tags, wish))
        .collect(Collectors.toList());

    Comparator<Cand> cmp = Comparator
        .<Cand>comparingInt(c -> -overlap(wish, c.tags))
        .thenComparing(c -> c.precio);

    var ordered = filtrados.stream().sorted(cmp).collect(Collectors.toList());
    log.debug("[Copilot] wish={}, total={}, filtrados={}", wish, base.size(), ordered.size());
    return ordered;
  }

  private List<Cand> obtenerCandidatosSoloListado() {
    Pageable pageReq = PageRequest.of(0, 200, Sort.by("nombre").ascending());
    return destinoRepo.findAll(pageReq).getContent().stream().map(this::toCandidate).toList();
  }

  /* ===================== Helpers ===================== */

  private Cand toCandidate(Destino d) {
    Integer id = resolveDestinoId(d);
    String nombre = safeInvoke(d, "getNombre");
    String pais   = safeInvoke(d, "getPais");
    String desc   = safeInvoke(d, "getDescripcion");

    BigDecimal precioReal = null;
    try {
      if (id != null) precioReal = paqueteRepo.findMinPrecioByDestinoId(id);
    } catch (Exception e) {
      log.debug("No se pudo obtener min(precio) para destino {}", id, e);
    }
    if (precioReal == null) {
      precioReal = BigDecimal.valueOf(estimacionDeCosto(desc, pais));
    }

    List<String> tags = inferirTags(nombre, desc);
    String imagen = resolveImageUrl(d); // <-- toma la imagen del registro (campo 'imagen')

    return new Cand(id, nombre, pais, precioReal, tags, imagen);
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

  private static Integer resolveDestinoId(Object d) {
    try { return (Integer) d.getClass().getMethod("getDestinoId").invoke(d); } catch (Exception ignore) {}
    try { return (Integer) d.getClass().getMethod("getIdDestino").invoke(d); } catch (Exception ignore) {}
    try { return (Integer) d.getClass().getMethod("getId").invoke(d); } catch (Exception ignore) {}
    return null;
  }

  private static String safeInvoke(Object d, String getter) {
    try {
      Object v = d.getClass().getMethod(getter).invoke(d);
      return v != null ? String.valueOf(v) : null;
    } catch (Exception e) {
      return null;
    }
  }

  /** Resuelve la URL de imagen probando getters; prioriza 'getImagen()'. */
  private String resolveImageUrl(Object d) {
    return firstNonBlank(
        safeInvoke(d, "getImagen"),       // <-- tu campo real
        safeInvoke(d, "getImagenUrl"),
        safeInvoke(d, "getImageUrl"),
        safeInvoke(d, "getUrlImagen"),
        safeInvoke(d, "getFotoPortada"),
        safeInvoke(d, "getFoto"),
        safeInvoke(d, "getImagenPrincipal")
    );
  }

  private static String firstNonBlank(String... vals) {
    if (vals == null) return null;
    for (String v : vals) {
      if (v != null && !v.isBlank()) return v.trim();
    }
    return null;
  }

  /** Lee min/max; si no existen, cae a presupuestoUSD único. */
  private static BigDecimal readBudget(PreferenciasViaje prefs, boolean isMin) {
    Number n = (Number) reflectGet(prefs, isMin ? "getPresupuestoMinUSD" : "getPresupuestoMaxUSD");
    if (n == null) n = (Number) reflectGet(prefs, "getPresupuestoUSD");
    return (n == null) ? null : new BigDecimal(n.toString());
  }
  private static Object reflectGet(Object obj, String method) {
    try { return obj.getClass().getMethod(method).invoke(obj); }
    catch (Exception e) { return null; }
  }

  /* ======= estimación & tags ======= */

  private static int estimacionDeCosto(String desc, String pais) {
    String txt = ((desc == null ? "" : desc) + " " + (pais == null ? "" : pais)).toLowerCase(Locale.ROOT);
    int base = 1000;
    if (txt.contains("méxico") || txt.contains("mexico")) base -= 100;
    if (txt.contains("japón")  || txt.contains("japon"))  base += 400;
    if (txt.contains("perú")   || txt.contains("peru"))   base += 150;
    if (txt.contains("honduras")) base -= 500;
    return Math.max(200, base);
  }

  private static List<String> inferirTags(String nombre, String desc) {
    String txt = ((nombre == null ? "" : nombre) + " " + (desc == null ? "" : desc)).toLowerCase(Locale.ROOT);
    List<String> tags = new ArrayList<>();
    if (txt.contains("playa") || txt.contains("isla")) tags.add("playa");
    if (txt.contains("lago")) { tags.add("playa"); tags.add("naturaleza"); }
    if (txt.contains("ruina") || txt.contains("hist") || txt.contains("imperio") ||
        txt.contains("museo") || txt.contains("catedral") || txt.contains("arqueolog"))
      tags.add("cultura");
    if (txt.contains("natur") || txt.contains("parque") || txt.contains("reserva") ||
        txt.contains("sender") || txt.contains("montañ") || txt.contains("volcán") ||
        txt.contains("volcan") || txt.contains("selva") || txt.contains("bosque") ||
        txt.contains("cascada"))
      tags.add("naturaleza");
    if (txt.contains("nocturn") || txt.contains("bar") || txt.contains("discoteca"))
      tags.add("vidaNocturna");
    if (txt.contains("gastron") || txt.contains("comida") || txt.contains("restaurante"))
      tags.add("gastronomía");
    if (tags.isEmpty()) tags.add("turismo");
    return tags.stream().map(String::toLowerCase).distinct().toList();
  }

  /* ===================== Fallback defensivo ===================== */

  private String buildFallbackText(PreferenciasViaje prefs, List<Cand> cand) {
    List<String> wish = Optional.ofNullable(prefs.getTipo()).orElse(List.of())
        .stream().filter(Objects::nonNull)
        .map(s -> s.toLowerCase(Locale.ROOT))
        .map(TravelCopilotService::norm)
        .toList();

    // Blindaje: si hay wish, volver a filtrar aquí también
    List<Cand> base = (wish.isEmpty())
        ? cand
        : cand.stream().filter(c -> hasAnyTag(c.tags, wish)).toList();

    Comparator<Cand> cmp = Comparator
        .<Cand>comparingInt(c -> -overlap(wish, c.tags))
        .thenComparing(c -> c.precio);

    List<Cand> ordenados = base.stream().sorted(cmp).toList();

    StringBuilder sb = new StringBuilder("**(Fallback sin IA)** Recomendaciones según tu BD:\n");
    int i = 1;
    for (Cand c : ordenados) {
      sb.append(i++).append(") ")
        .append(c.nombre != null ? c.nombre : "(sin nombre)")
        .append(" — ")
        .append(c.pais != null ? c.pais : "");
      if (c.precio != null) sb.append(" — costo ~USD ").append(c.precio.stripTrailingZeros().toPlainString());
      sb.append("\n   • Coincidencia: ").append(c.tags).append("\n");
    }
    if (ordenados.isEmpty()) sb.append("(sin coincidencias con los filtros)\n");
    return sb.toString();
  }

  /* ===================== Matching helpers ===================== */

  private static boolean hasAnyTag(List<String> candidateTags, List<String> wish) {
    if (wish == null || wish.isEmpty()) return true;
    if (candidateTags == null || candidateTags.isEmpty()) return false;

    Set<String> cand = candidateTags.stream()
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .map(TravelCopilotService::norm)
        .collect(Collectors.toSet());

    for (String w : wish) if (cand.contains(norm(w))) return true;
    return false;
  }

  private static int overlap(List<String> a, List<String> b) {
    if (a == null || b == null) return 0;
    Set<String> setB = b.stream()
        .filter(Objects::nonNull)
        .map(String::toLowerCase)
        .map(TravelCopilotService::norm)
        .collect(Collectors.toSet());
    int c = 0;
    for (String x : a) if (setB.contains(norm(x))) c++;
    return c;
  }

  /** Quita acentos y espacios; minúsculas. */
  private static String norm(String s) {
    if (s == null) return "";
    String n = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    return n.toLowerCase(Locale.ROOT).replace(" ", "");
  }

  // Descripción sencilla y estable (no IA)
  private static String buildDescripcion(PreferenciasViaje prefs){
    String tipos = (prefs.getTipo()==null || prefs.getTipo().isEmpty())
        ? "viaje general"
        : String.join(", ", prefs.getTipo());
    String comp  = (prefs.getCompaneros()==null || prefs.getCompaneros().isBlank())
        ? "cualquier compañía" : prefs.getCompaneros();
    Integer d    = prefs.getDuracionDias();
    String rango = null;
    try {
      Number min = (Number) reflectGet(prefs, "getPresupuestoMinUSD");
      Number max = (Number) reflectGet(prefs, "getPresupuestoMaxUSD");
      if (min!=null && max!=null) rango = "con presupuesto entre USD " + min + " y USD " + max;
      else {
        Number uno = (Number) reflectGet(prefs, "getPresupuestoUSD");
        if (uno!=null) rango = "con presupuesto cercano a USD " + uno;
      }
    } catch (Exception ignore) { }

    StringBuilder sb = new StringBuilder("Recomendado para ");
    sb.append(tipos).append(". Ideal para viajar ");
    sb.append(d!=null && d>0 ? ("por "+d+" días ") : "");
    sb.append("con ").append(comp).append(rango!=null?(", "+rango):".");
    return sb.toString();
  }
}
