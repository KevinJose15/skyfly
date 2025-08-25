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

    // Redirección inicial
    @GetMapping("/")
    public String redirectToBienvenida() {
        return "redirect:/bienvenida";
    }

    @GetMapping("/bienvenida")
    public String bienvenida() {
        return "Home/bienvenida";
    }

    @GetMapping("/home")
    public String home() {
        return "Home/index";
    }

    @GetMapping("/login")
    public String login() {
        return "Home/formLogin";
    }

    // 👉 Vista de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("cliente", new Cliente());
        return "Home/registro";
    }

    // 👉 Procesar registro
    @PostMapping("/registro")
    public String registrarCuenta(@ModelAttribute Usuario usuario,
                                  @ModelAttribute Cliente cliente) {
        // Configuramos el usuario
        usuario.setRol(Rol.Cliente);
        usuario.setStatus(true);
        Usuario usuarioGuardado = usuarioService.crearOeditar(usuario);

        // Asociamos cliente a usuario
        cliente.setUsuario(usuarioGuardado);
        clienteService.crearOeditar(cliente);

        return "redirect:/login?registroExitoso=true";
    }
}
