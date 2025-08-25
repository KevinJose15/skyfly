package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.MetodoPago;
import esfe.skyfly.Modelos.CodigoConfirmacion;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Utilidades.LuhnValidator;
import esfe.skyfly.Servicios.Interfaces.IPagoService;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import esfe.skyfly.Servicios.Interfaces.IMetodoPagoService;
import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.text.NumberFormat;
import java.util.Locale;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private IPagoService pagoService;

    @Autowired
    private IReservaService reservaService;

    @Autowired
    private CodigoConfirmacionService codigoConfirmacionService;

    @Autowired
    private IMetodoPagoService metodoPagoService;

    // LISTAR PAGOS
    @GetMapping
    public String index(Model model) {
        try {
            model.addAttribute("pagos", pagoService.obtenerTodos());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
        }
        return "pagos/index";
    }

    // OBTENER MONTO DE RESERVA (para llenar automáticamente)
    @GetMapping("/reserva/monto/{reservaId}")
    @ResponseBody
    public String obtenerMontoReserva(@PathVariable Integer reservaId) {
        Reservas reserva = reservaService.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        BigDecimal monto = (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null)
                ? reserva.getPaquete().getPrecio()
                : BigDecimal.ZERO;

        NumberFormat formatoDolar = NumberFormat.getCurrencyInstance(Locale.US);
        return formatoDolar.format(monto);
    }

    // FORMULARIO NUEVO PAGO
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("reservas", reservaService.obtenerTodos());
        model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
        model.addAttribute("action", "create");
        return "pagos/mant";
    }

    // FORMULARIO EDITAR PAGO
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Pago pago = pagoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        model.addAttribute("pago", pago);
        model.addAttribute("reservas", reservaService.obtenerTodos());
        model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
        model.addAttribute("action", "edit");
        return "pagos/mant";
    }

    // VER PAGO
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Pago pago = pagoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
        model.addAttribute("pago", pago);
        model.addAttribute("action", "view");
        return "pagos/mant";
    }

    // ELIMINAR PAGO (GET)
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        Pago pago = pagoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));
        model.addAttribute("pago", pago);
        model.addAttribute("action", "delete");
        return "pagos/mant";
    }

    // GUARDAR NUEVO PAGO
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Pago pago, BindingResult result, Model model,
                            RedirectAttributes redirect) {

        // Validar tarjeta
        if (pago.getNumeroTarjeta() == null || !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
            result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
        }

        // Hidratar reserva y monto
        if (pago.getReservaId() != null) {
            Reservas reserva = reservaService.buscarPorId(pago.getReservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            pago.setReserva(reserva);

if (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null) {
    pago.setMonto(reserva.getPaquete().getPrecio()); // ya es BigDecimal
} else {
    pago.setMonto(BigDecimal.ZERO);
}
        } else {
            result.rejectValue("reservaId", "error.reservaId", "Debes seleccionar una reserva");
        }

        // Hidratar método de pago
        if (pago.getMetodoPagoId() != null) {
            pago.setMetodoPago(metodoPagoService.buscarPorId(pago.getMetodoPagoId())
                    .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado")));
        } else {
            result.rejectValue("metodoPagoId", "error.metodoPagoId", "Debes seleccionar un método de pago");
        }

        // Si hay errores, volver a la vista
        if (result.hasErrors()) {
            model.addAttribute("reservas", reservaService.obtenerTodos());
            model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
            model.addAttribute("action", "create");
            return "pagos/mant";
        }

        // Guardar últimos 4 dígitos de tarjeta
        if (pago.getNumeroTarjeta() != null && pago.getNumeroTarjeta().length() >= 4) {
            pago.setUltimos4Tarjeta(pago.getNumeroTarjeta()
                    .substring(pago.getNumeroTarjeta().length() - 4));
        }

        // Datos de pago
        pago.setCodigoAutorizacion(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstadoPago(EstadoReserva.PENDIENTE);

        // Guardar pago
        pagoService.crearOeditar(pago);

        // Generar código de confirmación
        String emailCliente = pago.getReserva().getCliente().getUsuario().getEmail();
        CodigoConfirmacion codigo = codigoConfirmacionService.crearCodigo(emailCliente);

        redirect.addFlashAttribute("msg", "Pago registrado ✅. Código de confirmación enviado a: "
                + emailCliente + " (Código: " + codigo.getCodigo() + ")");
        return "redirect:/codigo";
    }

    // GUARDAR EDICIÓN
    @PostMapping("/edit")
    public String saveEditado(@ModelAttribute Pago pago, BindingResult result, Model model,
                              RedirectAttributes redirect) {

        // Hidratar reserva y método de pago
        if (pago.getReservaId() != null) {
            pago.setReserva(reservaService.buscarPorId(pago.getReservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada")));
        }

        if (pago.getMetodoPagoId() != null) {
            pago.setMetodoPago(metodoPagoService.buscarPorId(pago.getMetodoPagoId())
                    .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado")));
        }

        // Validar tarjeta
        if (pago.getNumeroTarjeta() != null && !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
            result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
        }

        if (result.hasErrors()) {
            model.addAttribute("reservas", reservaService.obtenerTodos());
            model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
            model.addAttribute("action", "edit");
            return "pagos/mant";
        }

        // Guardar últimos 4 dígitos de tarjeta
        if (pago.getNumeroTarjeta() != null && pago.getNumeroTarjeta().length() >= 4) {
            pago.setUltimos4Tarjeta(pago.getNumeroTarjeta()
                    .substring(pago.getNumeroTarjeta().length() - 4));
        }

        pagoService.crearOeditar(pago);
        redirect.addFlashAttribute("msg", "Pago actualizado correctamente");
        return "redirect:/pagos";
    }

    // ELIMINAR POST
    @PostMapping("/delete")
    public String deletePost(@ModelAttribute Pago pago, RedirectAttributes redirect) {
        pagoService.eliminarPorId(pago.getPagoId());
        redirect.addFlashAttribute("msg", "Pago eliminado correctamente");
        return "redirect:/pagos";
    }
}
