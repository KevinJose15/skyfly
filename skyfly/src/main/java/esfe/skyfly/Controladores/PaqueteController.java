package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Paquete;

import esfe.skyfly.Servicios.Interfaces.IPaqueteService;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/paquetes")
public class PaqueteController {

    @Autowired
    private IPaqueteService paqueteService;

    @Autowired
    private IDestinoService destinoService;

    // ----------- INDEX (LISTADO + PAGINACIÓN) --------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Paquete> paquetesPage = paqueteService.buscarTodos(pageable);

        model.addAttribute("paquetes", paquetesPage);

        int totalPages = paquetesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "paquete/index"; // 👈 la vista debe estar en templates/paquete/index.html
    }

    // ----------- CREAR --------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("paquete", new Paquete());
        model.addAttribute("destinos", destinoService.buscarTodos());
        model.addAttribute("action", "create");
        return "paquete/mant";
    }

    // ----------- EDITAR --------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Paquete paquete = paqueteService.buscarPorId(id).orElseThrow();
        model.addAttribute("paquete", paquete);
        model.addAttribute("destinos", destinoService.buscarTodos());
        model.addAttribute("action", "edit");
        return "paquete/mant";
    }

    // ----------- VER (solo lectura) --------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Paquete paquete = paqueteService.buscarPorId(id).orElseThrow();
        model.addAttribute("paquete", paquete);
        model.addAttribute("destinos", destinoService.buscarTodos());
        model.addAttribute("action", "view");
        return "paquete/mant";
    }

    // ----------- ELIMINAR (confirmación) --------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Paquete paquete = paqueteService.buscarPorId(id).orElseThrow();
        model.addAttribute("paquete", paquete);
        model.addAttribute("destinos", destinoService.buscarTodos());
        model.addAttribute("action", "delete");
        return "paquete/mant";
    }

    // ----------- PROCESAR CREAR --------------
    @PostMapping("/create")
    public String saveNuevo(@Valid @ModelAttribute Paquete paquete, BindingResult result,
                            RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("destinos", destinoService.buscarTodos());
            model.addAttribute("action", "create");
            return "paquete/mant";
        }

        paqueteService.crearOeditar(paquete);
        redirect.addFlashAttribute("msg", "Paquete creado correctamente");
        return "redirect:/paquetes";
    }

    // ----------- PROCESAR EDITAR --------------
    @PostMapping("/edit")
    public String saveEditado(@Valid @ModelAttribute Paquete paquete, BindingResult result,
                              RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("destinos", destinoService.buscarTodos());
            model.addAttribute("action", "edit");
            return "paquete/mant";
        }

        paqueteService.crearOeditar(paquete);
        redirect.addFlashAttribute("msg", "Paquete actualizado correctamente");
        return "redirect:/paquetes";
    }

    // ----------- PROCESAR ELIMINAR --------------
    @PostMapping("/delete")
public String deletePaquete(@ModelAttribute Paquete paquete, RedirectAttributes redirect) {
    try {
        paqueteService.eliminarPorId(paquete.getPaqueteId());
        redirect.addFlashAttribute("msg", "Paquete eliminado correctamente");

    } catch (DataIntegrityViolationException e) {
        redirect.addFlashAttribute("error", "No se puede eliminar este paquete porque está siendo usado en reservas");

    } catch (Exception e) {
        redirect.addFlashAttribute("error", "Ocurrió un error al eliminar el paquete");
    }

    return "redirect:/paquetes";
}

}
