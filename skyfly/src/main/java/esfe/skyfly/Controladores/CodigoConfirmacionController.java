package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Servicios.Interfaces.IPagoService;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/codigo")
public class CodigoConfirmacionController {

    private static final Logger log = LoggerFactory.getLogger(CodigoConfirmacionController.class);

    private final CodigoConfirmacionService codigoConfirmacionService;
    private final IPagoService pagoService;
    private final IReservaService reservaService;

    public CodigoConfirmacionController(CodigoConfirmacionService codigoConfirmacionService,
                                        IPagoService pagoService,
                                        IReservaService reservaService) {
        this.codigoConfirmacionService = codigoConfirmacionService;
        this.pagoService = pagoService;
        this.reservaService = reservaService;
    }

    // Landing opcional
    @GetMapping
    public String index(Model model) {
        return "codigo/index"; // templates/codigo/index.html (si lo usas)
    }

    // Form de validación
    @GetMapping("/validar")
    public String validarView() {
        return "codigo/mant"; // templates/codigo/mant.html
    }

    // Validar código (CLIENTE)
    @PostMapping("/validar")
    public String validarCodigo(@RequestParam("codigo") String codigo,
                                Authentication authentication,
                                RedirectAttributes redirect) {
        try {
            if (authentication == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticación requerida");
            }

            final String email = authentication.getName();
            log.debug("Validando código para email={} codigo={}", email, codigo);

            boolean valido = codigoConfirmacionService.validarCodigo(email, codigo);
            log.debug("Resultado validación: {}", valido);

            if (valido) {
                // Buscar el último pago pendiente del cliente
                pagoService.buscarUltimoPagoPendientePorCliente(email).ifPresentOrElse(pago -> {
                    log.debug("Pago pendiente encontrado: {}", pago.getPagoId());

                    // Confirmar pago
                    pago.setEstadoPago(EstadoReserva.CONFIRMADA);
                    pagoService.crearOeditar(pago);

                    // Confirmar reserva asociada (vía objeto, no por id transitorio)
                    Reservas reserva = (pago.getReserva() != null)
                            ? pago.getReserva()
                            : reservaService.buscarPorId(pago.getReservaId())
                                  .orElse(null);

                    if (reserva != null) {
                        log.debug("Reserva asociada: {}", reserva.getReservaId());
                        reserva.setEstado(EstadoReserva.CONFIRMADA);
                        reservaService.crearOeditar(reserva);
                    } else {
                        log.warn("No se pudo localizar la reserva asociada al pago {}", pago.getPagoId());
                    }
                }, () -> {
                    log.warn("No hay pago pendiente para {}", email);
                });

                redirect.addFlashAttribute("msg", "✅ Código válido. Pago y reserva confirmados.");
            } else {
                redirect.addFlashAttribute("msg", "❌ Código inválido o ya usado.");
            }

        } catch (ResponseStatusException ex) {
            throw ex; // deja que Security/ControllerAdvice lo maneje
        } catch (Exception e) {
            log.error("Error al validar código", e);
            redirect.addFlashAttribute("msg", "⚠ Error interno: " + e.getMessage());
        }

        // Mantén al cliente en su funnel
        return "redirect:/pagos/index-cliente";
    }
}
