package esfe.skyfly.Controladores;

import esfe.skyfly.Repositorios.IDestinoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/media")
public class MediaController {

  private final IDestinoRepository destinoRepo;

  public MediaController(IDestinoRepository destinoRepo) {
    this.destinoRepo = destinoRepo;
  }

  /**
   * Sirve la imagen del destino directamente desde BD.
   * Responde 404 si no hay imagen.
   */
  @Transactional(readOnly = true)
  @GetMapping("/destinos/{id}")
  public ResponseEntity<byte[]> destinoImage(@PathVariable Integer id) {
    byte[] bytes;
    try {
      bytes = destinoRepo.findImagenById(id);  // <-- consulta solo la columna del BLOB
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    if (bytes == null || bytes.length == 0) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    String mime = sniff(bytes);
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
        .contentType(MediaType.parseMediaType(mime))
        .body(bytes);
  }

  /** Detección simple del tipo de imagen por firma binaria. */
  private static String sniff(byte[] b) {
    if (b == null || b.length < 12) return "image/jpeg";
    if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) return "image/jpeg";                 // JPEG
    if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png"; // PNG
    if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F') return "image/gif";                         // GIF
    if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
        && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";    // WEBP
    return "image/jpeg";
  }
}
