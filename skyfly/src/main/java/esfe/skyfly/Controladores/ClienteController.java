package esfe.skyfly.Controladores;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
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
    private IUsuarioService usuarioService; // necesario para filtrar usuarios con rol CLIENTE

    // ----------- INDEX (LISTADO + PAGINACIÓN) --------------
    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "page") Optional<Integer> page,
                        @RequestParam(value = "size") Optional<Integer> size) {
        int currentPage = Math.max(0, page.orElse(1) - 1);
        int pageSize = Math.max(1, size.orElse(5));

        Pageable pageable = PageRequest.of(currentPage, pageSize);
        Page<Cliente> clientesPage = clienteService.buscarTodos(pageable);

        model.addAttribute("clientes", clientesPage);

        int totalPages = clientesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed().collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Asegúrate de tener templates/clientes/index.html
        return "clientes/index";
    }

    // ----------- ALIAS LISTADO --------------
    @GetMapping("/mant") // opcional: alias solo para listar
    public String mant(Model model,
                       @RequestParam(value = "page") Optional<Integer> page,
                       @RequestParam(value = "size") Optional<Integer> size) {
        return index(model, page, size);
    }

    // ----------- CREAR --------------
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("cliente", new Cliente());

        // Cargar solo usuarios con rol CLIENTE y opcionalmente libres (no asignados a Cliente)
        List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
        model.addAttribute("usuarios", usuarios);

        model.addAttribute("action", "create");
        return "clientes/mant"; // Asegúrate de tener templates/clientes/mant.html
    }

    // ----------- EDITAR --------------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        model.addAttribute("cliente", cliente);

        List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
        model.addAttribute("usuarios", usuarios);

        model.addAttribute("action", "edit");
        return "clientes/mant";
    }

    // ----------- VER (solo lectura) --------------
    @GetMapping("/view/{id}")
    public String view(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        model.addAttribute("cliente", cliente);
        model.addAttribute("action", "view");
        return "clientes/mant";
    }

    // ----------- ELIMINAR (confirmación) --------------
    @GetMapping("/delete/{id}")
    public String deleteConfirm(@PathVariable Integer id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        model.addAttribute("cliente", cliente);
        model.addAttribute("action", "delete");
        return "clientes/mant";
    }

    // ----------- PROCESAR CREATE --------------
    @PostMapping("/create")
    public String saveNuevo(@ModelAttribute Cliente cliente, BindingResult result,
                            RedirectAttributes redirect, Model model) {

        // Validar selección de usuario
        if (cliente.getUsuario() == null || cliente.getUsuario().getId() == null) {
            result.rejectValue("usuario", "error.usuario", "Debes seleccionar un usuario");
        }

        // Cargar el usuario real desde la BD y validar rol
        if (!result.hasFieldErrors("usuario")) {
            Usuario usuario = usuarioService.buscarPorId(cliente.getUsuario().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

            if (usuario.getRol() != Rol.Cliente) {
                result.rejectValue("usuario", "error.usuario", "El usuario seleccionado no tiene rol CLIENTE");
            } else {
                cliente.setUsuario(usuario);
            }
        }

        if (result.hasErrors()) {
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "create");
            return "clientes/mant";
        }

        try {
            clienteService.crearOeditar(cliente);
        } catch (DataIntegrityViolationException ex) {
            // Por si el usuario ya está ligado a otro Cliente (único)
            result.rejectValue("usuario", "error.usuario", "El usuario ya está asignado a un cliente");
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "create");
            return "clientes/mant";
        }

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

        // Cargar el usuario real desde la BD y validar rol
        if (!result.hasFieldErrors("usuario")) {
            Usuario usuario = usuarioService.buscarPorId(cliente.getUsuario().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario no encontrado"));

            if (usuario.getRol() != Rol.Cliente) {
                result.rejectValue("usuario", "error.usuario", "El usuario seleccionado no tiene rol CLIENTE");
            } else {
                cliente.setUsuario(usuario);
            }
        }

        if (result.hasErrors()) {
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "edit");
            return "clientes/mant";
        }

        try {
            clienteService.crearOeditar(cliente);
        } catch (DataIntegrityViolationException ex) {
            result.rejectValue("usuario", "error.usuario", "El usuario ya está asignado a un cliente");
            List<Usuario> usuarios = usuarioService.findByRol(Rol.Cliente);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("action", "edit");
            return "clientes/mant";
        }

        redirect.addFlashAttribute("msg", "Cliente actualizado correctamente");
        return "redirect:/clientes";
    }

    // ----------- PROCESAR DELETE --------------
    @PostMapping("/delete")
    public String deleteCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirect) {
        if (cliente.getClienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de cliente requerido");
        }
        clienteService.eliminarPorId(cliente.getClienteId());
        redirect.addFlashAttribute("msg", "Cliente eliminado correctamente");
        return "redirect:/clientes";
    }
}