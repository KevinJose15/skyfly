package esfe.skyfly.Controladores;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Servicios.Interfaces.IFacturaService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    private final IFacturaService facturaService;

    public FacturaController(IFacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("facturas", facturaService.listar());
        return "facturas/index";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Factura factura = facturaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        model.addAttribute("factura", factura);
        return "facturas/detalle";
    }

    @GetMapping("/{id}/pdf")
    public void descargarPdf(@PathVariable Integer id, HttpServletResponse response) {
        Factura factura = facturaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));

        byte[] pdf = facturaService.generarPdf(factura);
        String filename = "Factura-" + factura.getIdFactura() + ".pdf";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded);
        response.setContentLength(pdf.length);
        try {
            response.getOutputStream().write(pdf);
            response.flushBuffer();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo escribir el PDF", e);
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id) {
        facturaService.eliminar(id);
        return "redirect:/facturas";
    }
}