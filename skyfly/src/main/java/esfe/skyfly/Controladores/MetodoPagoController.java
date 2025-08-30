package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.MetodoPago;
import esfe.skyfly.Servicios.Interfaces.IMetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<MetodoPago> metodoPagoPage = metodoPagoService.buscarTodos(pageable);

        model.addAttribute("metodos", metodoPagoPage);

        int totalPages = metodoPagoPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "metodopago/index";
    }

    
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("metodoPago", new MetodoPago());
        model.addAttribute("action", "create");
        return "metodopago/mant";
    }

    
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id).orElseThrow(
                () -> new IllegalArgumentException("ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "edit");
        return "metodopago/mant";
    }

    
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id).orElseThrow(
                () -> new IllegalArgumentException("ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "view");
        return "metodopago/mant";
    }

    
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        MetodoPago metodoPago = metodoPagoService.buscarPorId(id).orElseThrow(
                () -> new IllegalArgumentException("ID de Método de Pago inválido: " + id));
        model.addAttribute("metodoPago", metodoPago);
        model.addAttribute("action", "delete");
        return "metodopago/mant";
    }

    
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute MetodoPago metodoPago, BindingResult result,
                            RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "create");
            return "metodopago/mant";
        }
        metodoPagoService.crearOeditar(metodoPago);
        redirect.addFlashAttribute("msg", "Método de Pago creado correctamente");
        return "redirect:/metodopago";
    }

    
    @PostMapping("/edit")
    public String saveEditado(@ModelAttribute MetodoPago metodoPago, BindingResult result,
                              RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            return "metodopago/mant";
        }
        metodoPagoService.crearOeditar(metodoPago);
        redirect.addFlashAttribute("msg", "Método de Pago actualizado correctamente");
        return "redirect:/metodopago";
    }


    @PostMapping("/delete")
    public String deleteMetodoPago(@ModelAttribute MetodoPago metodoPago, RedirectAttributes redirect) {
        metodoPagoService.eliminarPorId(metodoPago.getMetodoPagoId());
        redirect.addFlashAttribute("msg", "Método de Pago eliminado correctamente");
        return "redirect:/metodopago";
    }
}