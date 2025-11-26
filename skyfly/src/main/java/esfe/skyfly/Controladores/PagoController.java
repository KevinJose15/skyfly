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
import esfe.skyfly.Servicios.Implementaciones.CodigoConfirmacionServiceImpl;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

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

    // IVA configurable (por ejemplo: 0.13 = 13%)
    @Value("${app.tax.rate:0.13}")
    private BigDecimal taxRate;

    // -----------------------------
    // LISTAR PAGOS (INDEX)
    // -----------------------------
   // ...
@GetMapping({"/", "/index"})
public String index(Authentication authentication, Model model) {
    boolean isCliente = authentication != null &&
        authentication.getAuthorities().stream().anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));
    if (isCliente) {
        return "redirect:/cliente/pagos";
    }

    // Back-office (agentes/admin) como ya lo tenías
    model.addAttribute("pagos", pagoService.obtenerTodos());
    String email = authentication != null ? authentication.getName() : null;
    Pago pagoPendiente = (email != null)
            ? pagoService.buscarUltimoPagoPendientePorCliente(email).orElse(null)
            : null;
    model.addAttribute("pagoPendiente", pagoPendiente);
    return "pagos/index";
}

// -----------------------------
// OBTENER MONTO BASE (SIN IVA)
// -----------------------------
@GetMapping("/reserva/monto/{reservaId}")
@ResponseBody
public String obtenerMontoReserva(@PathVariable Integer reservaId) {
    Reservas reserva = reservaService.buscarPorId(reservaId)
            .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

    BigDecimal montoBase = (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null)
            ? reserva.getPaquete().getPrecio()
            : BigDecimal.ZERO;

    NumberFormat formatoDolar = NumberFormat.getCurrencyInstance(Locale.US);
    return formatoDolar.format(montoBase); 
}


    // -----------------------------
    // FORMULARIO NUEVO PAGO
    // -----------------------------
   @GetMapping("/create")
public String create(@RequestParam(value = "reservaId", required = false) Integer reservaId,
                     Authentication auth,
                     Model model) {

    boolean isCliente = auth != null &&
            auth.getAuthorities().stream().anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));

    // Traemos todas y, si es Cliente, filtramos solo las suyas
    List<Reservas> todas = reservaService.obtenerTodos();
    List<Reservas> visibles = todas;
    if (isCliente) {
        String email = auth.getName();
        visibles = todas.stream()
                .filter(r -> r.getCliente() != null
                        && r.getCliente().getUsuario() != null
                        && email.equalsIgnoreCase(r.getCliente().getUsuario().getEmail()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Pago a bindear
    Pago p = new Pago();

    // Preselección: query param; si no hay y es cliente y solo tiene 1 → esa
    Integer selectedId = reservaId;
    if (selectedId == null && isCliente && visibles.size() == 1) {
        selectedId = visibles.get(0).getReservaId();
    }

    // 🔒 copia final para usar en lambdas
    final Integer selId = selectedId;

    if (selId != null) {
        p.setReservaId(selId);
    }

    // Texto para la píldora (usando la copia final)
    String resumenReserva = null;
    if (selId != null) {
        Reservas sel = visibles.stream()
                .filter(r -> r.getReservaId().equals(selId))
                .findFirst()
                .orElseGet(() -> todas.stream()
                        .filter(r -> r.getReservaId().equals(selId))
                        .findFirst()
                        .orElse(null));

        if (sel != null) {
            String clienteName = (sel.getCliente() != null && sel.getCliente().getUsuario() != null)
                    ? ((sel.getCliente().getUsuario().getName() != null && !sel.getCliente().getUsuario().getName().isBlank())
                        ? sel.getCliente().getUsuario().getName()
                        : sel.getCliente().getUsuario().getEmail())
                    : "N/D";

            String paqueteName = (sel.getPaquete() != null && sel.getPaquete().getNombre() != null)
                    ? sel.getPaquete().getNombre()
                    : "N/D";

            resumenReserva = "ID: " + sel.getReservaId()
                    + " | Cliente: " + clienteName
                    + " | Paquete: " + paqueteName;
        }
    }

    model.addAttribute("pago", p);
    model.addAttribute("reservas", visibles); // agentes ven todas; clientes solo las suyas
    model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
    model.addAttribute("action", "create");
    model.addAttribute("isCliente", isCliente);
    model.addAttribute("resumenReserva", resumenReserva);

    return "pagos/mant";
}

    // -----------------------------
    // VER PAGO
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
    // ELIMINAR PAGO
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
    // GUARDAR NUEVO PAGO
    // -----------------------------
   @PostMapping("/create")
public String saveNuevo(@ModelAttribute Pago pago, BindingResult result, Model model,
                        RedirectAttributes redirect, Authentication auth) {

    boolean isCliente = auth != null &&
            auth.getAuthorities().stream().anyMatch(a -> "ROLE_Cliente".equals(a.getAuthority()));

    // Validación de tarjeta (como ya tenías)
    if (pago.getNumeroTarjeta() == null || !LuhnValidator.isValid(pago.getNumeroTarjeta())) {
        result.rejectValue("numeroTarjeta", "error.numeroTarjeta", "Número de tarjeta inválido");
    }

    // Hidratar reserva + validación de pertenencia si es Cliente
    Reservas reserva = null;
    if (pago.getReservaId() != null) {
        reserva = reservaService.buscarPorId(pago.getReservaId())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // 🔐 Bloqueo: el cliente solo puede pagar reservas suyas
        if (isCliente) {
            String email = auth != null ? auth.getName() : null;
            boolean propia = (reserva.getCliente() != null
                    && reserva.getCliente().getUsuario() != null
                    && email != null
                    && email.equalsIgnoreCase(reserva.getCliente().getUsuario().getEmail()));
            if (!propia) {
                result.rejectValue("reservaId", "error.reservaId", "No puedes pagar reservas de otro cliente");
            }
        }

        pago.setReserva(reserva);

        // Aquí calculamos monto base + IVA y guardamos el monto TOTAL en pago.setMonto(...)
        BigDecimal montoBase = (reserva.getPaquete() != null && reserva.getPaquete().getPrecio() != null)
                ? reserva.getPaquete().getPrecio()
                : BigDecimal.ZERO;

        BigDecimal iva = calcularIva(montoBase);
        BigDecimal montoTotal = montoBase.add(iva).setScale(2, RoundingMode.HALF_UP);

        // Guardamos el monto total (base + IVA) para que se muestre en "Mis pagos" y en el email
        pago.setMonto(montoTotal);
    } else {
        result.rejectValue("reservaId", "error.reservaId", "Debes seleccionar una reserva");
    }

    // Hidratar método de pago (igual que tu código)
    if (pago.getMetodoPagoId() != null) {
        pago.setMetodoPago(metodoPagoService.buscarPorId(pago.getMetodoPagoId())
                .orElseThrow(() -> new IllegalArgumentException("Método de pago no encontrado")));
    } else {
        result.rejectValue("metodoPagoId", "error.metodoPagoId", "Debes seleccionar un método de pago");
    }

    // Manejo de errores → recargamos el formulario con el mismo guardrail
    if (result.hasErrors()) {
        // reconstruimos el contexto visible
        java.util.List<Reservas> visibles = reservaService.obtenerTodos();
        if (isCliente) {
            String email = auth != null ? auth.getName() : null;
            visibles = visibles.stream()
                    .filter(r -> r.getCliente() != null
                            && r.getCliente().getUsuario() != null
                            && email != null
                            && email.equalsIgnoreCase(r.getCliente().getUsuario().getEmail()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("reservas", visibles);
        model.addAttribute("metodosPago", metodoPagoService.obtenerTodos());
        model.addAttribute("action", "create");
        model.addAttribute("isCliente", isCliente);
        // opcional: resumen para el pill si traía reservaId
        if (pago.getReservaId() != null) {
            Reservas sel = visibles.stream().filter(r -> r.getReservaId().equals(pago.getReservaId()))
                    .findFirst().orElse(null);
            if (sel != null) {
                String clienteName = (sel.getCliente() != null && sel.getCliente().getUsuario() != null)
                        ? (sel.getCliente().getUsuario().getName() != null && !sel.getCliente().getUsuario().getName().isBlank()
                            ? sel.getCliente().getUsuario().getName()
                            : sel.getCliente().getUsuario().getEmail())
                        : "N/D";
                String paqueteName = (sel.getPaquete() != null && sel.getPaquete().getNombre() != null)
                        ? sel.getPaquete().getNombre()
                        : "N/D";
                model.addAttribute("resumenReserva", "ID: " + sel.getReservaId()
                        + " | Cliente: " + clienteName + " | Paquete: " + paqueteName);
            }
        }
        return "pagos/mant";
    }

    // --- Resto igual a tu implementación original ---
    if (pago.getNumeroTarjeta() != null && pago.getNumeroTarjeta().length() >= 4) {
        pago.setUltimos4Tarjeta(pago.getNumeroTarjeta()
                .substring(pago.getNumeroTarjeta().length() - 4));
    }
    pago.setCodigoAutorizacion(java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    pago.setFechaPago(java.time.LocalDateTime.now());
    pago.setEstadoPago(EstadoReserva.PENDIENTE);

    pagoService.crearOeditar(pago);

    // -----------------------------
    // Preparar y enviar el código de confirmación por correo (sin mostrarlo en la vista)
    // -----------------------------
    // Recolectar datos desde la reserva de forma defensiva
    String emailParaEnvio = null;
    String nombreCliente = null;
    String nombrePaquete = null;
    LocalDateTime fechaReserva = null;
    BigDecimal montoTotal = pago.getMonto() != null ? pago.getMonto() : BigDecimal.ZERO;
    String destino = null;

    if (reserva != null) {
        if (reserva.getCliente() != null && reserva.getCliente().getUsuario() != null) {
            emailParaEnvio = reserva.getCliente().getUsuario().getEmail();
            nombreCliente = (reserva.getCliente().getUsuario().getName() != null && !reserva.getCliente().getUsuario().getName().isBlank())
                    ? reserva.getCliente().getUsuario().getName()
                    : emailParaEnvio;
        }

        if (reserva.getPaquete() != null) {
            nombrePaquete = reserva.getPaquete().getNombre();
            // destino: intenta desde paquete -> destino -> nombre
            if (reserva.getPaquete().getDestino() != null && reserva.getPaquete().getDestino().getNombre() != null) {
                destino = reserva.getPaquete().getDestino().getNombre();
            }
        }

        // Fecha: si tu entidad tiene fecha, úsala; si no, usa now()
        try {
            fechaReserva = reserva.getFechaReserva();
        } catch (Exception ex) {
            fechaReserva = LocalDateTime.now();
        }
    }

    // Si no hay email válido, avisamos y redirigimos sin intentar enviar correo
    if (emailParaEnvio == null || emailParaEnvio.isBlank()) {
        redirect.addFlashAttribute("msg",
                "Pago registrado ✅. No se pudo enviar el correo porque no existe una dirección de correo válida para la reserva.");
        return "redirect:/codigo";
    }

    // Llamada al servicio que crea el código, guarda en BD y envía el correo con plantilla.
    // Uso casteo a la implementación concreta para invocar crearCodigoYEnviarEmail si no quieres tocar la interfaz ahora.
    try {
        if (codigoConfirmacionService instanceof CodigoConfirmacionServiceImpl) {
            ((CodigoConfirmacionServiceImpl) codigoConfirmacionService).crearCodigoYEnviarEmail(
                    emailParaEnvio,
                    nombreCliente,
                    nombrePaquete,
                    fechaReserva,
                    montoTotal,
                    destino
            );
        } else {
            // fallback: si la implementación actual no es la esperada, utilizamos el método básico
            codigoConfirmacionService.crearCodigo(emailParaEnvio);
        }
    } catch (Exception ex) {
        // loguear si tienes logger o simplemente pasar el mensaje al flash para notificar al usuario
        redirect.addFlashAttribute("msg",
                "Pago registrado ✅. Ocurrió un problema al intentar enviar el correo de confirmación. " +
                        "El código fue generado y guardado en la base de datos.");
        return "redirect:/codigo";
    }

    redirect.addFlashAttribute("msg",
        "Pago registrado ✅. Hemos enviado un código de confirmación al correo: "
        + emailParaEnvio + ". Revisa tu bandeja de entrada o el correo no deseado.");
    return "redirect:/codigo";
}
    // -----------------------------
    // GUARDAR EDICIÓN
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

    // -----------------------------
    // Utilidades internas
    // -----------------------------
    private BigDecimal calcularIva(BigDecimal montoBase) {
        if (montoBase == null) return BigDecimal.ZERO;
        if (taxRate == null) taxRate = BigDecimal.valueOf(0.13);
        return montoBase.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
    }
}
