package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Modelos.Rol;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IClienteService clienteService;

    @GetMapping("/")
    public String redirectToBienvenida() { return "redirect:/bienvenida"; }

    @GetMapping("/bienvenida")
    public String bienvenida() { return "Home/bienvenida"; }

    @GetMapping("/login")
    public String login() { return "Home/formLogin"; }

    // Main de Admin/Agente (sirven el mismo layout)
@GetMapping("/admin/main")
public String mainAdmin() { return "layouts/_MainLayout"; }

@GetMapping("/agente/main")
public String mainAgente() { return "layouts/_MainLayout"; }

    // Index de Cliente
@GetMapping("/cliente/index")
public String indexCliente() { return "layouts/_MainLayout"; }

    // Registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("cliente", new Cliente());
        return "Home/registro";
    }

    @PostMapping("/registro")
    public String registrarCuenta(@ModelAttribute Usuario usuario,
                                  @ModelAttribute Cliente cliente) {
        usuario.setRol(Rol.Cliente);
        usuario.setStatus(true);
        Usuario usuarioGuardado = usuarioService.crearOeditar(usuario);

        cliente.setUsuario(usuarioGuardado);
        clienteService.crearOeditar(cliente);

        return "redirect:/login?registroExitoso=true";
    }
}