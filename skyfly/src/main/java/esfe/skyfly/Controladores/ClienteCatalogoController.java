package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
@RequestMapping("/cliente")
public class ClienteCatalogoController {

    private final IDestinoService destinoService;
    private final IPaqueteService paqueteService;

    public ClienteCatalogoController(IDestinoService destinoService, IPaqueteService paqueteService) {
        this.destinoService = destinoService;
        this.paqueteService = paqueteService;
    }

    @GetMapping("/destinos")
    public String destinos(Model model) {
        var destinos = destinoService.obtenerTodo();   // tu firma actual
        var paquetes = paqueteService.obtenerTodo();   // idem

        // Map<destinoId, precio mínimo "Desde">
        Map<Integer, BigDecimal> precioDesde = paquetes.stream()
                .filter(p -> p.getDestino() != null
                          && p.getDestino().getDestinoId() != null
                          && p.getPrecio() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDestino().getDestinoId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(Paquete::getPrecio)),
                                opt -> opt.map(Paquete::getPrecio).orElse(null)
                        )
                ));

        model.addAttribute("destinos", destinos);
        model.addAttribute("paquetes", paquetes);       // para listar paquetes por destino
        model.addAttribute("precioDesde", precioDesde); // “Desde $…”
        return "cliente/catalogo_destinos";
    }

    // ===== Imagen destino =====
    @GetMapping(value = "/destinos/{id}/img")
    public ResponseEntity<byte[]> imagen(@PathVariable Integer id) {
        Optional<Destino> opt = destinoService.buscarPorId(id);
        if (opt.isEmpty() || opt.get().getImagen() == null || opt.get().getImagen().length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        byte[] data = opt.get().getImagen();
        String contentType = detectarContentType(data);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(data);
    }

    private String detectarContentType(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            String ct = URLConnection.guessContentTypeFromStream(bais);
            return (ct != null && ct.startsWith("image/")) ? ct : MediaType.IMAGE_JPEG_VALUE;
        } catch (IOException e) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
    }
}