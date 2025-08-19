
package esfe.skyfly.Controladores;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
 @Autowired
    private IUsuarioService usuarioService;

@GetMapping
public String index(Model model,
                    @RequestParam(value = "page") Optional<Integer> page,
                    @RequestParam(value = "size") Optional<Integer> size) {
    int currentPage = page.orElse(1) - 1;
    int pageSize = size.orElse(5);

    Pageable pageable = PageRequest.of(currentPage, pageSize); // si quieres ver lo último primero: PageRequest.of(currentPage, pageSize, Sort.by(Sort.Direction.DESC,"id"))

    Page<Usuario> usuariosPage = usuarioService.buscarTodos(pageable);

    // IMPORTANTE: la vista espera un Page en "usuarios"
    model.addAttribute("usuarios", usuariosPage);

    int totalPages = usuariosPage.getTotalPages();
    if (totalPages > 0) {
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed().collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
    }

    return "usuario/index";
}

    // ----------- CREAR --------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("action", "create");
        return "usuario/mant";
    }

    // ----------- EDITAR --------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "edit");
        return "usuario/mant";
    }

    // ----------- VER (solo lectura) --------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "view");
        return "usuario/mant";
    }

    // ----------- ELIMINAR (confirmación) --------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id).orElseThrow();
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "delete");
        return "usuario/mant";
    }

    // ----------- PROCESAR POST según action --------------
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Usuario usuario, BindingResult result,
                            RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "create");
            return "usuario/mant";
        }
        usuarioService.crearOeditar(usuario);
        redirect.addFlashAttribute("msg", "Usuario creado correctamente");
        return "redirect:/usuarios";
    }

    @PostMapping("/edit")
    public String saveEditado(@ModelAttribute Usuario usuario, BindingResult result,
                              RedirectAttributes redirect, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("action", "edit");
            return "usuario/mant";
        }
        usuarioService.crearOeditar(usuario);
        redirect.addFlashAttribute("msg", "Usuario actualizado correctamente");
        return "redirect:/usuarios";
    }

    @PostMapping("/delete")
    public String deleteUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirect) {
        usuarioService.eliminarPorId(usuario.getId());
        redirect.addFlashAttribute("msg", "Usuario eliminado correctamente");
        return "redirect:/usuarios";
    }
}