package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.CodigoConfirmacion;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/codigo")
public class CodigoConfirmacionController {

    private final CodigoConfirmacionService codigoConfirmacionService;

    public CodigoConfirmacionController(CodigoConfirmacionService codigoConfirmacionService) {
        this.codigoConfirmacionService = codigoConfirmacionService;
    }

    // Vista inicial (index)
    @GetMapping
public String index(Model model) {
    return "codigo/index";
}


    // Generar código
    @PostMapping("/generar")
    public String generarCodigo(@RequestParam("email") String email, Model model) {
        CodigoConfirmacion nuevo = codigoConfirmacionService.crearCodigo(email);
        model.addAttribute("msg", "Código generado y enviado a: " + nuevo.getEmail() +
                " (Código: " + nuevo.getCodigo() + ")");
        return "codigo/index";
    }

    // Vista de validación
    @GetMapping("/validar")
    public String validarView() {
        return "codigo/mant"; // Muestra formulario para validar código
    }

    // Validar código
    @PostMapping("/validar")
    public String validarCodigo(@RequestParam("email") String email,
                                @RequestParam("codigo") String codigo,
                                Model model) {
        boolean valido = codigoConfirmacionService.validarCodigo(email, codigo);

        if (valido) {
            model.addAttribute("msg", "✅ Código válido. Confirmación exitosa.");
        } else {
            model.addAttribute("msg", "❌ Código inválido o ya usado.");
        }

        return "codigo/mant";
    }
}