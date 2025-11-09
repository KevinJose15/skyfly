package esfe.skyfly.ai.port;

import esfe.skyfly.ai.dto.*;
import java.util.List;

public interface DestinoCandidateProvider {
  List<DestinoCandidato> topCandidates(PreferenciasViaje prefs, int limit);
}
