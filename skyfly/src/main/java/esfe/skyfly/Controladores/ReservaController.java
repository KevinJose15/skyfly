package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;
import esfe.skyfly.Utilidades.SecurityUtils;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired private IReservaService reservaService;
    @Autowired private IClienteService clienteService;
    @Autowired private IPaqueteService paqueteService;

    // Para obtener el cliente logueado
    @Autowired private SecurityUtils securityUtils;

    // =======================
    // LISTADO (ADMIN/AGENTE)
    // =======================
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = Math.max(0, page.orElse(1) - 1);
        int pageSize    = Math.max(1, size.orElse(5));

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Reservas> reservasPage = reservaService.buscarTodos(pageable);

        model.addAttribute("reservas", reservasPage);

        int totalPages = reservasPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // -> templates/reserva/index.html
        return "reserva/index";
    }

    // Alias admin/agente si proteges /reservas/mant/**
    @GetMapping("/mant")
    public String mant(Model model,
                       @RequestParam(value = "page") Optional<Integer> page,
                       @RequestParam(value = "size") Optional<Integer> size) {
        return index(model, page, size);
    }

    // ===================================
    // LISTADO (CLIENTE) - SOLO SUS DATOS
    // ===================================
    // -> templates/cliente/reservas/index.html
    @GetMapping("/index-cliente")
    public String reservasCliente(Model model) {
        Cliente cli = securityUtils.getClienteActual();
        if (cli == null) return "redirect:/login";

        // Si no hay método dedicado, filtramos en memoria (batch razonable)
        List<Reservas> propias = reservaService.buscarTodos(PageRequest.of(0, 1000))
                .getContent().stream()
                .filter(r -> r.getCliente() != null
                          && r.getCliente().getClienteId().equals(cli.getClienteId()))
                .collect(Collectors.toList());

        model.addAttribute("reservas", propias);
        return "cliente/reservas/index";
    }

    // ==========================
    // CREATE (ADMIN/AGENTE)
    // ==========================
    @GetMapping("/create")
    public String create(Model model) {
        Reservas r = new Reservas();
        r.setFechaReserva(LocalDateTime.now());
        r.setEstado(EstadoReserva.PENDIENTE);
        model.addAttribute("reserva", r);
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("paquetes", paqueteService.obtenerTodo());
        model.addAttribute("action", "create");
        return "reserva/mant";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada: " + id));
        model.addAttribute("reserva", reserva);
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("paquetes", paqueteService.obtenerTodo());
        model.addAttribute("action", "edit");
        return "reserva/mant";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada: " + id));
        model.addAttribute("reserva", reserva);
        model.addAttribute("action", "view");
        return "reserva/mant";
    }

    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada: " + id));
        model.addAttribute("reserva", reserva);
        model.addAttribute("action", "delete");
        return "reserva/mant";
    }

    @PostMapping("/create")
    public String saveNuevo(@Valid @ModelAttribute("reserva") Reservas reserva,
                            BindingResult result,
                            RedirectAttributes redirect, Model model) {

        // Reconstruir cliente
        if (reserva.getCliente() != null && reserva.getCliente().getClienteId() != null) {
            Cliente cliente = clienteService.buscarPorId(reserva.getCliente().getClienteId()).orElse(null);
            if (cliente == null) result.rejectValue("cliente", "error.cliente", "Cliente inválido");
            else reserva.setCliente(cliente);
        } else {
            result.rejectValue("cliente", "error.cliente", "Debes seleccionar un cliente");
        }

        // Reconstruir paquete
        if (reserva.getPaquete() != null && reserva.getPaquete().getPaqueteId() != null) {
            Paquete paquete = paqueteService.buscarPorId(reserva.getPaquete().getPaqueteId()).orElse(null);
            if (paquete == null) result.rejectValue("paquete", "error.paquete", "Paquete inválido");
            else reserva.setPaquete(paquete);
        } else {
            result.rejectValue("paquete", "error.paquete", "Debes seleccionar un paquete");
        }

        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.obtenerTodos());
            model.addAttribute("paquetes", paqueteService.obtenerTodo());
            model.addAttribute("action", "create");
            return "reserva/mant";
        }

        if (reserva.getEstado() == null) reserva.setEstado(EstadoReserva.PENDIENTE);
        if (reserva.getFechaReserva() == null) reserva.setFechaReserva(LocalDateTime.now());

        reservaService.crearOeditar(reserva);
        redirect.addFlashAttribute("msg", "Reserva creada correctamente");
        return "redirect:/reservas";
    }

    @PostMapping("/edit")
    public String saveEditado(@Valid @ModelAttribute("reserva") Reservas reserva,
                              BindingResult result,
                              RedirectAttributes redirect, Model model) {

        if (reserva.getCliente() == null || reserva.getCliente().getClienteId() == null)
            result.rejectValue("cliente", "error.cliente", "Debes seleccionar un cliente");

        if (reserva.getPaquete() == null || reserva.getPaquete().getPaqueteId() == null)
            result.rejectValue("paquete", "error.paquete", "Debes seleccionar un paquete");

        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.obtenerTodos());
            model.addAttribute("paquetes", paqueteService.obtenerTodo());
            model.addAttribute("action", "edit");
            return "reserva/mant";
        }

        reservaService.crearOeditar(reserva);
        redirect.addFlashAttribute("msg", "Reserva actualizada correctamente");
        return "redirect:/reservas";
    }

    @PostMapping("/delete")
    public String deleteReserva(@ModelAttribute Reservas reserva, RedirectAttributes redirect) {
        if (reserva.getReservaId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de reserva requerido");

        reservaService.eliminarPorId(reserva.getReservaId());
        redirect.addFlashAttribute("msg", "Reserva eliminada correctamente");
        return "redirect:/reservas";
    }

    // =======================================
    // CREATE (CLIENTE) — AUTOSELECCIÓN
    // =======================================
    // -> templates/cliente/reservas/create.html
    // Enlázalo desde cliente/paquetes/index.html con ?paqueteId=...
    @GetMapping("/create-cliente")
    public String createCliente(@RequestParam(required = false) Integer paqueteId, Model model) {
        Cliente cli = securityUtils.getClienteActual();
        if (cli == null) return "redirect:/login";

        Reservas r = new Reservas();
        r.setCliente(cli);
        r.setFechaReserva(LocalDateTime.now());
        r.setEstado(EstadoReserva.PENDIENTE);

        if (paqueteId != null) {
            Paquete p = paqueteService.buscarPorId(paqueteId).orElse(null);
            r.setPaquete(p);
        }

        model.addAttribute("reserva", r);
        return "cliente/reservas/create";
    }

    @PostMapping("/create-cliente")
    public String saveCliente(@ModelAttribute("reserva") Reservas reserva,
                              BindingResult result,
                              RedirectAttributes redirect,
                              Model model) {
        Cliente cli = securityUtils.getClienteActual();
        if (cli == null) return "redirect:/login";

        // Blindaje: siempre el cliente logueado
        reserva.setCliente(cli);

        // Validar paquete (obligatorio en este flujo)
        if (reserva.getPaquete() == null || reserva.getPaquete().getPaqueteId() == null) {
            result.rejectValue("paquete", "error.paquete", "Debes seleccionar un paquete");
        } else {
            Paquete p = paqueteService.buscarPorId(reserva.getPaquete().getPaqueteId()).orElse(null);
            if (p == null) result.rejectValue("paquete", "error.paquete", "Paquete inválido");
            else reserva.setPaquete(p);
        }

        if (result.hasErrors()) {
            model.addAttribute("reserva", reserva);
            return "cliente/reservas/create";
        }

        if (reserva.getEstado() == null) reserva.setEstado(EstadoReserva.PENDIENTE);
        if (reserva.getFechaReserva() == null) reserva.setFechaReserva(LocalDateTime.now());

        Reservas guardada = reservaService.crearOeditar(reserva);
        redirect.addFlashAttribute("msg", "Reserva creada correctamente");
        // Siguiente paso del funnel: pago del cliente
        return "redirect:/pagos/create?reservaId=" + guardada.getReservaId();
    }
}
