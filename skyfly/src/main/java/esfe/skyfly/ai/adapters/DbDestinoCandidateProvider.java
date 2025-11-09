package esfe.skyfly.ai.adapters;

import esfe.skyfly.ai.dto.*;
import esfe.skyfly.ai.port.DestinoCandidateProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DbDestinoCandidateProvider implements DestinoCandidateProvider {

  @Override
  public List<DestinoCandidato> topCandidates(PreferenciasViaje prefs, int limit) {
    // TODO: Reemplaza con JPA/SQL real filtrando por presupuesto, mes (fechaViaje),
    // tags y clima. Aquí dejamos 2 mocks de referencia.
    List<DestinoCandidato> out = new ArrayList<>();

    out.add(DestinoCandidato.builder()
        .id(101L).nombre("Cartagena").pais("Colombia")
        .mejorEpoca(List.of("dic","ene","feb"))
        .costoMedioUSD(950)
        .tags(List.of("playa","historia","gastronomía"))
        .scorePopularidad(4.5).build());

    out.add(DestinoCandidato.builder()
        .id(205L).nombre("Cancún").pais("México")
        .mejorEpoca(List.of("nov","mar"))
        .costoMedioUSD(1100)
        .tags(List.of("playa","vidaNocturna"))
        .scorePopularidad(4.7).build());

    return out.subList(0, Math.min(limit, out.size()));
  }
}
