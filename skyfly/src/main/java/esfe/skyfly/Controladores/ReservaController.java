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

import esfe.skyfly.Modelos.Reservas;
import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.EstadoReserva;
import esfe.skyfly.Modelos.Paquete;
import esfe.skyfly.Servicios.Interfaces.IReservaService;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IPaqueteService;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private IReservaService reservaService;

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IPaqueteService paqueteService;

    // LISTADO
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Reservas> reservasPage = reservaService.buscarTodos(pageable);

        model.addAttribute("reservas", reservasPage);

        int totalPages = reservasPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "reserva/index";
    }

    // FORMULARIO CREATE
   @GetMapping("/create")
public String create(Model model) {
    Reservas r = new Reservas();
    r.setFechaReserva(LocalDateTime.now());
    r.setEstado(EstadoReserva.PENDIENTE); // fuerza estado pendiente
    model.addAttribute("reserva", r);
    model.addAttribute("clientes", clienteService.obtenerTodos());
    model.addAttribute("paquetes", paqueteService.obtenerTodo());
    model.addAttribute("action", "create");
    return "reserva/mant";
}


    // FORMULARIO EDIT
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        model.addAttribute("reserva", reserva);
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("paquetes", paqueteService.obtenerTodo());
        model.addAttribute("action", "edit");
        return "reserva/mant";
    }

    // VER
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        model.addAttribute("reserva", reserva);
        model.addAttribute("action", "view");
        return "reserva/mant";
    }

    // DELETE CONFIRM
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Reservas reserva = reservaService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        model.addAttribute("reserva", reserva);
        model.addAttribute("action", "delete");
        return "reserva/mant";
    }

  // GUARDAR NUEVO
@PostMapping("/create")
public String saveNuevo(@ModelAttribute Reservas reserva, BindingResult result,
                        RedirectAttributes redirect, Model model) {

    // Reconstruir cliente
    if (reserva.getCliente() != null && reserva.getCliente().getClienteId() != null) {
        Cliente cliente = clienteService.buscarPorId(reserva.getCliente().getClienteId())
                .orElse(null);
        reserva.setCliente(cliente);
    } else {
        result.rejectValue("cliente", "error.cliente", "Debes seleccionar un cliente");
    }

    // Reconstruir paquete
    if (reserva.getPaquete() != null && reserva.getPaquete().getPaqueteId() != null) {
        Paquete paquete = paqueteService.buscarPorId(reserva.getPaquete().getPaqueteId())
                .orElse(null);
        reserva.setPaquete(paquete);
    } else {
        result.rejectValue("paquete", "error.paquete", "Debes seleccionar un paquete");
    }

    if (result.hasErrors()) {
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("paquetes", paqueteService.obtenerTodo());
        model.addAttribute("action", "create");
        return "reserva/mant";
    }

    reservaService.crearOeditar(reserva);
    redirect.addFlashAttribute("msg", "Reserva creada correctamente");
    return "redirect:/reservas";
}

    // GUARDAR EDITADO
    @PostMapping("/edit")
    public String saveEditado(@ModelAttribute Reservas reserva, BindingResult result,
                              RedirectAttributes redirect, Model model) {

        if (reserva.getCliente() == null)
            result.rejectValue("cliente", "error.cliente", "Debes seleccionar un cliente");

        if (reserva.getPaquete() == null)
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

    // DELETE
    @PostMapping("/delete")
    public String deleteReserva(@ModelAttribute Reservas reserva, RedirectAttributes redirect) {
        reservaService.eliminarPorId(reserva.getReservaId());
        redirect.addFlashAttribute("msg", "Reserva eliminada correctamente");
        return "redirect:/reservas";
    }
}
