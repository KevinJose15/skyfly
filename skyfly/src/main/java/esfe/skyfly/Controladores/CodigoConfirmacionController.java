package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Servicios.Implementaciones.PagoServiceImpl;
import esfe.skyfly.Servicios.Implementaciones.ReservaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/codigo")
public class CodigoConfirmacionController {

    private final CodigoConfirmacionService codigoConfirmacionService;
    private final PagoServiceImpl pagoService;
    private final ReservaService reservaService;

    public CodigoConfirmacionController(
            CodigoConfirmacionService codigoConfirmacionService,
            PagoServiceImpl pagoService,
            ReservaService reservaService
    ) {
        this.codigoConfirmacionService = codigoConfirmacionService;
        this.pagoService = pagoService;
        this.reservaService = reservaService;
    }

    // Vista inicial (index)
    @GetMapping
    public String index(Model model) {
        return "codigo/index";
    }

    // Vista de validación del código
    @GetMapping("/validar")
    public String validarView() {
        return "codigo/mant"; // Formulario para validar código
    }

  // Validar código
@PostMapping("/validar")
public String validarCodigo(@RequestParam("codigo") String codigo,
                            Authentication authentication,
                            RedirectAttributes redirect) {
    try {
        String email = (authentication != null) ? authentication.getName() : "NO_AUTH";
        System.out.println("DEBUG - Email autenticado: " + email);
        System.out.println("DEBUG - Código recibido: " + codigo);

        boolean valido = codigoConfirmacionService.validarCodigo(email, codigo);
        System.out.println("DEBUG - Resultado validación: " + valido);

        if (valido) {
            pagoService.buscarUltimoPagoPendientePorCliente(email).ifPresent(pago -> {
                System.out.println("DEBUG - Pago encontrado: " + pago.getPagoId());

                pago.setEstadoPago(EstadoReserva.CONFIRMADA);
                pagoService.crearOeditar(pago);

                reservaService.buscarPorId(pago.getReservaId()).ifPresent(reserva -> {
                    System.out.println("DEBUG - Reserva encontrada: " + reserva.getReservaId());
                    reserva.setEstado(EstadoReserva.CONFIRMADA);
                    reservaService.crearOeditar(reserva);
                });
            });

            redirect.addFlashAttribute("msg", "✅ Código válido. Pago y reserva confirmados.");
        } else {
            redirect.addFlashAttribute("msg", "❌ Código inválido o ya usado.");
        }

    } catch (Exception e) {
        e.printStackTrace(); // 🔥 Esto mostrará la causa en consola
        redirect.addFlashAttribute("msg", "⚠ Error interno: " + e.getMessage());
    }

    return "redirect:/pagos/index";
}
}