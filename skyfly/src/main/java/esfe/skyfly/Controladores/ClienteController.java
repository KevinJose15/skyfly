package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Rol;
import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IUsuarioService usuarioService; // necesario para filtrar usuarios con rol Cliente

    // ----------- INDEX (LISTADO + PAGINACIÓN) --------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Cliente> clientesPage = clienteService.buscarTodos(pageable);

        model.addAttribute("clientes", clientesPage);

        int totalPages = clientesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "cliente/index"; // carpeta debe llamarse "cliente"
    }

    // ----------- NUEVO: ALIAS /clientes/mant (ADMIN/AGENTE) --------------
    @GetMapping("/mant")
    public String mant(Model model,
                       @RequestParam(value = "page") Optional<Integer> page,
                       @RequestParam(value = "size") Optional<Integer> size) {
        return index(model, page, size); // reutiliza el listado
    }

    // ----------- CREAR --------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("cliente", new Cliente());
        // cargamos solo usuarios con rol Cliente
        List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("action", "create");
        return "cliente/mant";
    }

    // ----------- EDITAR --------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("action", "edit");
        return "cliente/mant";
    }

    // ----------- VER (solo lectura) --------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        model.addAttribute("action", "view");
        return "cliente/mant";
    }

    // ----------- ELIMINAR (confirmación) --------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        model.addAttribute("action", "delete");
        return "cliente/mant";
    }

    // ----------- PROCESAR CREATE --------------
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Cliente cliente, BindingResult result,
                            RedirectAttributes redirect, Model model) {
        // Validar selección de usuario
        if (cliente.getUsuario() == null || cliente.getUsuario().getId() == null) {
            result.rejectValue("usuario", "error.usuario", "Debes seleccionar un usuario");
        }

        if (result.hasErrors()) {
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "create");
            return "cliente/mant";
        }

        // Cargar el usuario real desde la BD y asociarlo
        Usuario usuario = usuarioService.buscarPorId(cliente.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        cliente.setUsuario(usuario);

        clienteService.crearOeditar(cliente);
        redirect.addFlashAttribute("msg", "Cliente creado correctamente");
        return "redirect:/clientes";
    }

    // ----------- PROCESAR EDIT --------------
    @PostMapping("/edit")
    public String saveEditado(@ModelAttribute Cliente cliente, BindingResult result,
                              RedirectAttributes redirect, Model model) {
        // Validar selección de usuario
        if (cliente.getUsuario() == null || cliente.getUsuario().getId() == null) {
            result.rejectValue("usuario", "error.usuario", "Debes seleccionar un usuario");
        }

        if (result.hasErrors()) {
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "edit");
            return "cliente/mant";
        }

        // Cargar el usuario real desde la BD y asociarlo
        Usuario usuario = usuarioService.buscarPorId(cliente.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        cliente.setUsuario(usuario);

        clienteService.crearOeditar(cliente);
        redirect.addFlashAttribute("msg", "Cliente actualizado correctamente");
        return "redirect:/clientes";
    }

    // ----------- PROCESAR DELETE --------------
    @PostMapping("/delete")
    public String deleteCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirect) {
        clienteService.eliminarPorId(cliente.getClienteId()); // usar clienteId
        redirect.addFlashAttribute("msg", "Cliente eliminado correctamente");
        return "redirect:/clientes";
    }
}
