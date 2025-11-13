package esfe.skyfly.ai.adapters;

import esfe.skyfly.ai.dto.DestinoCandidato;
import esfe.skyfly.ai.dto.PreferenciasViaje;
import esfe.skyfly.ai.port.DestinoCandidateProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DbDestinoCandidateProvider implements DestinoCandidateProvider {

  @Override
  public List<DestinoCandidato> topCandidates(PreferenciasViaje prefs, int limit) {
    // TODO: Reemplazar por JPA/SQL real (filtros por presupuesto, mes, tags, clima)
    List<DestinoCandidato> out = new ArrayList<>();

    out.add(DestinoCandidato.builder()
        .id(101L)
        .nombre("Cartagena")
        .pais("Colombia")
        .mejorEpoca(List.of("dic", "ene", "feb"))

        .tags(List.of("playa", "historia", "gastronomía"))
        .scorePopularidad(4.5)
        .build());

    out.add(DestinoCandidato.builder()
        .id(205L)
        .nombre("Cancún")
        .pais("México")
        .mejorEpoca(List.of("nov", "mar"))
        .tags(List.of("playa", "vidaNocturna"))
        .scorePopularidad(4.7)
        .build());

    return out.subList(0, Math.min(limit, out.size()));
  }
}
