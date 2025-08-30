package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import esfe.skyfly.config.ClienteActualProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente/reservas")
public class ClienteReservaController {

    private final ClienteActualProvider clienteActualProvider;
    private final IReservaService reservaService;
    private final IPaqueteService paqueteService;

    public ClienteReservaController(ClienteActualProvider cap, IReservaService rs, IPaqueteService ps) {
        this.clienteActualProvider = cap;
        this.reservaService = rs;
        this.paqueteService = ps;
    }

@GetMapping
public String create(@RequestParam(required = false) Integer paqueteId,
                     @RequestParam(required = false) Integer destinoId,
                     Model model) {

    Cliente cliente = clienteActualProvider.getClienteActual();

    Reservas r = new Reservas();
    r.setCliente(cliente);
    r.setFechaReserva(java.time.LocalDateTime.now());
    try { r.setEstado(esfe.skyfly.Modelos.EstadoReserva.PENDIENTE); } catch (Exception ignored) {}

    // 1) Trae SIEMPRE la lista de paquetes
    java.util.List<esfe.skyfly.Modelos.Paquete> paquetes = paqueteService.obtenerTodo(); // <-- si tu firma es obtenerTodos(), cámbiala

    // 2) Filtra por destinoId si vino
    if (destinoId != null) {
        paquetes = paquetes.stream()
                .filter(p -> p.getDestino() != null
                          && p.getDestino().getDestinoId() != null
                          && p.getDestino().getDestinoId().equals(destinoId))
                .collect(java.util.stream.Collectors.toList());
    }

    // 3) Preselecciona si vino paqueteId (pero NO toques la lista)
    if (paqueteId != null) {
        esfe.skyfly.Modelos.Paquete pre = paqueteService.buscarPorId(paqueteId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Paquete no encontrado"));
        r.setPaquete(pre); // con esto el select se marcará solo
    } else {
        // garantiza objeto para el binding del select
        if (r.getPaquete() == null) {
            try { r.setPaquete(esfe.skyfly.Modelos.Paquete.class.getDeclaredConstructor().newInstance()); }
            catch (Exception ignored) {}
        }
    }

    model.addAttribute("paquetes", paquetes); // lista completa SIEMPRE
    model.addAttribute("reserva", r);
    model.addAttribute("action", "create");
    return "cliente/reservas";
}



    // ====== CREATE (POST) => /cliente/reservas ======
    @PostMapping
    public String saveNuevo(@ModelAttribute("reserva") Reservas reserva,
                            RedirectAttributes redirect, Model model) {

        // Fuerza dueño, fecha y estado
        reserva.setCliente(clienteActualProvider.getClienteActual());
        if (reserva.getFechaReserva() == null) {
            reserva.setFechaReserva(LocalDateTime.now());
        }
        try { reserva.setEstado(EstadoReserva.PENDIENTE); } catch (Exception ignored) {}

        // Normaliza paquete
        if (reserva.getPaquete() == null || reserva.getPaquete().getPaqueteId() == null) {
            model.addAttribute("reserva", reserva);
            model.addAttribute("paquetes", paqueteService.obtenerTodo());
            model.addAttribute("action", "create");
            return "cliente/reservas";
        } else {
            Paquete p = paqueteService.buscarPorId(reserva.getPaquete().getPaqueteId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Paquete inválido"));
            reserva.setPaquete(p);
        }

        // Persiste (tu método)
        reservaService.crearOeditar(reserva);

        redirect.addFlashAttribute("msg", "Reserva creada correctamente");
       return "redirect:/pagos/create?reservaId=" + reserva.getReservaId();
    }
}