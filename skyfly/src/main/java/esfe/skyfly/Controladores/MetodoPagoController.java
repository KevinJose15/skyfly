package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.MetodoPago;
import esfe.skyfly.Servicios.Interfaces.IMetodoPagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/metodopago")
public class MetodoPagoController {

    @Autowired
    private IMetodoPagoService metodoPagoService;

    // ----------- INDEX (ADMIN/AGENTE) --------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = Math.max(0, page.orElse(1) - 1);
        int pageSize    = Math.max(1, size.orElse(5));

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<MetodoPago> metodoPagoPage = metodoPagoService.buscarTodos(pageable);

        model.addAttribute("metodos", metodoPagoPage);

        int totalPages = metodoPagoPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Asegúrate de tener templates/metodopago/index.html
        return "metodopago/index";
    }

    // ----------- CREATE (form) --------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("metodoPago", new MetodoPago());
        model.addAttribute("action", "create");
        // -> templates/metodopago/mant.html
        return "metodopago/mant";
    }

    // ----------- EDIT (form) --------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "edit");
        return "metodopago/mant";
    }

    // ----------- VIEW (solo lectura) --------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "view");
        return "metodopago/mant";
    }

    // ----------- DELETE (confirmación) --------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "delete");
        return "metodopago/mant";
    }

    // ----------- SAVE CREATE --------------
    @PostMapping("/create")
    public String saveNuevo(@Valid @ModelAttribute("metodoPago") MetodoPago metodoPago,
                            BindingResult result,
                            RedirectAttributes redirect,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "create");
            return "metodopago/mant";
        }
        try {
            metodoPagoService.crearOeditar(metodoPago);
        } catch (DataIntegrityViolationException ex) {
            result.reject("unique", "Ya existe un método de pago con esos datos");
            model.addAttribute("action", "create");
            return "metodopago/mant";
        }
        redirect.addFlashAttribute("msg", "Método de Pago creado correctamente");
        return "redirect:/metodopago";
    }

    // ----------- SAVE EDIT --------------
    @PostMapping("/edit")
    public String saveEditado(@Valid @ModelAttribute("metodoPago") MetodoPago metodoPago,
                              BindingResult result,
                              RedirectAttributes redirect,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            return "metodopago/mant";
        }
        try {
            metodoPagoService.crearOeditar(metodoPago);
        } catch (DataIntegrityViolationException ex) {
            result.reject("unique", "Ya existe un método de pago con esos datos");
            model.addAttribute("action", "edit");
            return "metodopago/mant";
        }
        redirect.addFlashAttribute("msg", "Método de Pago actualizado correctamente");
        return "redirect:/metodopago";
    }

    // ----------- DELETE (POST) --------------
    @PostMapping("/delete")
    public String deleteMetodoPago(@ModelAttribute("metodoPago") MetodoPago metodoPago,
                                   RedirectAttributes redirect) {
        if (metodoPago.getMetodoPagoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de Método de Pago requerido");
        }
        metodoPagoService.eliminarPorId(metodoPago.getMetodoPagoId());
        redirect.addFlashAttribute("msg", "Método de Pago eliminado correctamente");
        return "redirect:/metodopago";
    }
}
