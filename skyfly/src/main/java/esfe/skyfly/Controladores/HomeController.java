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

    // (Compatibilidad) Si en alguna parte quedó /home, evita 404
    @GetMapping("/home")
    public String homeFallback() {
        return "redirect:/bienvenida";
    }

    @GetMapping("/bienvenida")
    public String bienvenida() {
        return "Home/bienvenida";
    }

    @GetMapping("/login")
    public String login() {
        return "Home/formLogin";
    }

    // 👉 Vista para administrador
    @GetMapping("/admin/main")
    public String mainAdmin() {
        return "_MainLayout"; // layout de administración
    }

    // 👉 Vista para agente
    @GetMapping("/agente/main")
    public String mainAgente() {
        return "_MainLayout"; // mismo layout para agente
    }

    // 👉 Vista para cliente
    @GetMapping("/cliente/index")
    public String indexCliente() {
        return "Cliente/index"; // landing del cliente (destinos/paquetes)
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
