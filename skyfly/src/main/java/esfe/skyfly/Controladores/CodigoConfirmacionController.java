package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Servicios.Implementaciones.PagoServiceImpl;
import esfe.skyfly.Servicios.Implementaciones.ReservaService;
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
public String validarCodigo(@RequestParam("email") String email,
                            @RequestParam("codigo") String codigo,
                            RedirectAttributes redirect) {

    boolean valido = codigoConfirmacionService.validarCodigo(email, codigo);

    if (valido) {
        // 🔹 Buscar último pago del cliente por email
        Pago pago = pagoService.buscarPorEmailCliente(email);

        if (pago != null) {
            // Confirmar pago
            pago.setEstadoPago(EstadoReserva.CONFIRMADA);
            pagoService.crearOeditar(pago); // save actualizado

            // Confirmar reserva asociada
            Reservas reserva = reservaService.buscarPorId(pago.getReservaId()).orElse(null);
            if (reserva != null) {
                reserva.setEstado(EstadoReserva.CONFIRMADA);
                reservaService.crearOeditar(reserva); // save actualizado
            }
        }

        redirect.addFlashAttribute("msg", " Código válido. Pago y reserva confirmados.");
    } else {
        redirect.addFlashAttribute("msg", "❌ Código inválido o ya usado.");
    }

    return "redirect:/pagos";
}
}