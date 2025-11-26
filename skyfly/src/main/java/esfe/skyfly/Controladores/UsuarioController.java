package esfe.skyfly.Controladores;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService usuarioService;

    // ----------------- LISTAR -----------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Usuario> usuariosPage = usuarioService.buscarTodos(pageable);

        model.addAttribute("usuarios", usuariosPage);

        int totalPages = usuariosPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                                                 .boxed()
                                                 .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "usuario/index";
    }

    // ----------------- CREAR -----------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("action", "create");
        return "usuario/mant";
    }

@PostMapping("/create")
public String saveNuevo(@ModelAttribute Usuario usuario, BindingResult result,
                        RedirectAttributes redirect, Model model) {

    if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isBlank()) {
        result.rejectValue("passwordHash", "error.usuario", "La contraseña es obligatoria");
    }

    // Validación: email duplicado usando el nuevo método del servicio
    if (usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
        Optional<Usuario> existente = usuarioService.buscarPorEmail(usuario.getEmail());
        if (existente.isPresent()) {
            // si es creación (id null) -> duplicado; si es edición -> duplicado solo si id distinto
            if (usuario.getId() == null || !usuario.getId().equals(existente.get().getId())) {
                result.rejectValue("email", "error.usuario", "Ya existe un usuario con ese correo");
            }
        }
    }

    if (result.hasErrors()) {
        model.addAttribute("action", "create");
        return "usuario/mant";
    }

    try {
        usuarioService.crearOeditar(usuario);
        redirect.addFlashAttribute("msg", "Usuario creado correctamente");
    } catch (org.springframework.dao.DataIntegrityViolationException ex) {
        // log.error("Error creando usuario", ex);
        redirect.addFlashAttribute("err", "No se pudo crear el usuario: conflicto de datos (email duplicado u otro).");
        return "redirect:/usuarios";
    } catch (Exception ex) {
        // log.error("Error creando usuario", ex);
        redirect.addFlashAttribute("err", "Error al crear usuario: " + ex.getMessage());
        return "redirect:/usuarios";
    }

    return "redirect:/usuarios";
}

    // ----------------- EDITAR -----------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "edit");
        return "usuario/mant";
    }

// ----------------- EDITAR -----------------
@PostMapping("/edit")
public String saveEditado(@ModelAttribute Usuario usuario, BindingResult result,
                          RedirectAttributes redirect, Model model) {

    // Obtener el usuario existente de la base de datos
    Usuario usuarioExistente = usuarioService.buscarPorId(usuario.getId())
                                  .orElseThrow();

    // Mantener la contraseña actual si el campo está vacío
    if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
        usuario.setPasswordHash(usuarioExistente.getPasswordHash());
    }

    // Nota: ya no usamos result.rejectValue aquí

    usuarioService.crearOeditar(usuario);
    redirect.addFlashAttribute("msg", "Usuario actualizado correctamente");
    return "redirect:/usuarios";
}

    // ----------------- VER -----------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "view");
        return "usuario/mant";
    }

    // ----------------- ELIMINAR -----------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("action", "delete");
        return "usuario/mant";
    }

   @PostMapping("/delete")
public String deleteUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirect) {

    try {
        usuarioService.eliminarPorId(usuario.getId());
        redirect.addFlashAttribute("msg", "Usuario eliminado correctamente");

    } catch (DataIntegrityViolationException ex) {

        // Este es el caso de clave foránea (usuario usado en cliente)
        redirect.addFlashAttribute("error", "No se puede eliminar el usuario porque está asociado a un cliente.");

    } catch (Exception ex) {

        // Error inesperado
        redirect.addFlashAttribute("err", "Error inesperado al eliminar el usuario.");

    }

    return "redirect:/usuarios";
}

}