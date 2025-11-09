package esfe.skyfly.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiChatService {
  private static final Logger log = LoggerFactory.getLogger(GeminiChatService.class);
  private static final String MODEL = "gemini-2.5-flash";
  private final Client client;

  public GeminiChatService(@Value("${gemini.api.key:}") String keyFromProps) {
    String apiKey = firstNonBlank(keyFromProps, System.getenv("GOOGLE_API_KEY"), System.getenv("GEMINI_API_KEY"));
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("No se encontró API key (gemini.api.key | GOOGLE_API_KEY | GEMINI_API_KEY).");
    }
    log.info("GeminiChatService: API key cargada.");
    this.client = Client.builder().apiKey(apiKey).build();
  }

  public String generatePlainText(String prompt) {
    try {
      GenerateContentResponse resp = client.models.generateContent(MODEL, prompt, null);
      return resp.text();
    } catch (Exception e) {
      throw new AiUnavailableException("Gemini no disponible: " + e.getMessage(), e);
    }
  }

  private static String firstNonBlank(String... vals){ if(vals==null) return null; for(String v: vals) if(v!=null && !v.isBlank()) return v; return null; }
}
