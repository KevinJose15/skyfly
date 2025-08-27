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

import org.springframework.security.core.Authentication;
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
import java.util.List;
import java.util.stream.Collectors;

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

    // -----------------------------
    // LISTAR PAGOS (ADMIN/AGENTE)
    // -----------------------------
    @GetMapping({"/", "/index"})
    public String index(Authentication authentication, Model model) {

        // Todos los pagos para la tabla (uso administrativo)
        model.addAttribute("pagos", pagoService.obtenerTodos());

        // Último pago pendiente del cliente logeado (si aplica)
        String email = authentication.getName();
        Pago pagoPendiente = pagoService.buscarUltimoPagoPendientePorCliente(email)
                                        .orElse(null);
        model.addAttribute("pagoPendiente", pagoPendiente);

        return "pagos/index";
    }

    // 👉 NUEVO: alias para admin/agente si tu SecurityConfig usa /pagos/mant/**
    @GetMapping("/mant")
    public String mant(Authentication authentication, Model model) {
        return index(authentication, model);
    }

    // -----------------------------
    // LISTAR PAGOS DEL CLIENTE (CLIENTE)
    // -----------------------------
    // Vista: templates/Cliente/pagos/index.html
    @GetMapping("/index-cliente")
    public String indexCliente(Authentication authentication, Model model) {
        String email = authentication.getName();

        // Si tu service tuviera un "findByClienteEmail", úsalo.
        // Aquí filtramos en memoria para no tocar tu service:
        List<Pago> propios = pagoService.obtenerTodos().stream()
            .filter(p -> p.getReserva() != null
                      && p.getReserva().getCliente() != null
                      && p.getReserva().getCliente().getUsuario() != null
                      && email.equalsIgnoreCase(p.getReserva().getCliente().getUsuario().getEmail()))
            .collect(Collectors.toList());

        model.addAttribute("pagos", propios);

        // También puedes mostrar su último pendiente si lo deseas:
        Pago pagoPendiente = pagoService.buscarUltimoPagoPendientePorCliente(email).orElse(null);
        model.addAttribute("pagoPendiente", pagoPendiente);

        return "Cliente/pagos/index";
    }

    // -----------------------------
    // OBTENER MONTO DE RESERVA (AJAX)
    // -----------------------------
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

    // -----------------------------
    // FORMULARIO NUEVO PAGO (ADMIN/AGENTE)
    // -----------------------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("pago", new Pago());
        model.addAttribute("reservas", reservaService.obtenerTodos());
        model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
        model.addAttribute("action", "create");
        return "pagos/mant";
    }

    // -----------------------------
    // FORMULARIO EDITAR PAGO (ADMIN/AGENTE)
    // -----------------------------
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

    // -----------------------------
    // VER PAGO (ADMIN/AGENTE)
    // -----------------------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Pago pago = pagoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        model.addAttribute("pago", pago);
        model.addAttribute("action", "view");
        return "pagos/mant";
    }

    // -----------------------------
    // ELIMINAR PAGO (ADMIN/AGENTE)
    // -----------------------------
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        Pago pago = pagoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        model.addAttribute("pago", pago);
        model.addAttribute("action", "delete");
        return "pagos/mant";
    }

    @PostMapping("/delete")
    public String deletePost(@ModelAttribute Pago pago, RedirectAttributes redirect) {
        pagoService.eliminarPorId(pago.getPagoId());
        redirect.addFlashAttribute("msg", "Pago eliminado correctamente");
        return "redirect:/pagos/index";
    }

    // -----------------------------
    // GUARDAR NUEVO PAGO (ADMIN/AGENTE)
    // -----------------------------
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Pago pago, BindingResult result, Model model,
                            RedirectAttributes redirect) {

        // Validar tarjeta
        if (pago.getNumeroTarjeta() == null || !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
            result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
        }

        // Hidratar reserva
        if (pago.getReservaId() != null) {
            Reservas reserva = reservaService.buscarPorId(pago.getReservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            pago.setReserva(reserva);

            if (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null) {
                pago.setMonto(reserva.getPaquete().getPrecio());
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

        pagoService.crearOeditar(pago);

        // Generar código de confirmación
        String emailCliente = pago.getReserva().getCliente().getUsuario().getEmail();
        CodigoConfirmacion codigo = codigoConfirmacionService.crearCodigo(emailCliente);

        redirect.addFlashAttribute("msg", "Pago registrado ✅. Código de confirmación enviado a: "
                + emailCliente + " (Código: " + codigo.getCodigo() + ")");
        return "redirect:/codigo";
    }

    // -----------------------------
    // GUARDAR EDICIÓN (ADMIN/AGENTE)
    // -----------------------------
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

        if (pago.getNumeroTarjeta() != null && !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
            result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
        }

        if (result.hasErrors()) {
            model.addAttribute("reservas", reservaService.obtenerTodos());
            model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
            model.addAttribute("action", "edit");
            return "pagos/mant";
        }

        // Guardar últimos 4 dígitos
        if (pago.getNumeroTarjeta() != null && pago.getNumeroTarjeta().length() >= 4) {
            pago.setUltimos4Tarjeta(pago.getNumeroTarjeta()
                    .substring(pago.getNumeroTarjeta().length() - 4));
        }

        pagoService.crearOeditar(pago);
        redirect.addFlashAttribute("msg", "Pago actualizado correctamente");
        return "redirect:/pagos/index";
    }

    // =====================================================
    // NUEVO: FLUJO DEL CLIENTE (CREATE con reserva preseleccionada)
    // =====================================================

    // Formulario de pago para CLIENTE (reserva ya elegida)
    // Vista: templates/Cliente/pagos/create.html
    @GetMapping("/create-cliente")
    public String createCliente(@RequestParam Integer reservaId, Model model) {
        Reservas reserva = reservaService.buscarPorId(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Pago pago = new Pago();
        pago.setReserva(reserva);

        model.addAttribute("pago", pago);
        model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
        // No exponemos lista de reservas aquí; la reserva ya viene fijada.
        return "Cliente/pagos/create";
    }

    // Procesar pago del CLIENTE
    @PostMapping("/create-cliente")
    public String saveNuevoCliente(@ModelAttribute Pago pago, BindingResult result, Model model,
                                   RedirectAttributes redirect) {

        // Validar tarjeta
        if (pago.getNumeroTarjeta() == null || !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
            result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
        }

        // Hidratar reserva (obligatoria)
        if (pago.getReserva() == null || pago.getReserva().getReservaId() == null) {
            result.rejectValue("reserva.reservaId", "error.reserva", "Reserva inválida");
        } else {
            Reservas reserva = reservaService.buscarPorId(pago.getReserva().getReservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
            pago.setReserva(reserva);

            if (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null) {
                pago.setMonto(reserva.getPaquete().getPrecio());
            } else {
                pago.setMonto(BigDecimal.ZERO);
            }
        }

        // Hidratar método de pago
        if (pago.getMetodoPagoId() != null) {
            pago.setMetodoPago(metodoPagoService.buscarPorId(pago.getMetodoPagoId())
                    .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado")));
        } else {
            result.rejectValue("metodoPagoId", "error.metodoPagoId", "Debes seleccionar un método de pago");
        }

        if (result.hasErrors()) {
            model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
            return "Cliente/pagos/create";
        }

        // Guardar últimos 4
        if (pago.getNumeroTarjeta() != null && pago.getNumeroTarjeta().length() >= 4) {
            pago.setUltimos4Tarjeta(
                pago.getNumeroTarjeta().substring(pago.getNumeroTarjeta().length() - 4));
        }

        // Datos de pago
        pago.setCodigoAutorizacion(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstadoPago(EstadoReserva.PENDIENTE);

        pagoService.crearOeditar(pago);

        // Código de confirmación
        String emailCliente = pago.getReserva().getCliente().getUsuario().getEmail();
        CodigoConfirmacion codigo = codigoConfirmacionService.crearCodigo(emailCliente);

        redirect.addFlashAttribute("msg",
            "Pago registrado ✅. Código de confirmación enviado a: " + emailCliente +
            " (Código: " + codigo.getCodigo() + ")");

        // Redirige al listado del cliente
        return "redirect:/pagos/index-cliente";
    }
}
