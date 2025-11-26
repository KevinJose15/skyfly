package esfe.skyfly.Controladores;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    // HomeController
@GetMapping("/main")
public String main() {
    return "layouts/_MainLayout"; // ojo con mayúsculas/minúsculas
}

    // Registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("cliente", new Cliente());
        return "Home/registro";
    }
@PostMapping("/registro")
public String registrarCuenta(@ModelAttribute("usuario") Usuario usuario,
                              BindingResult result,
                              @ModelAttribute("cliente") Cliente cliente,
                              Model model) {

    // ✅ Validar correo duplicado
    if (usuarioService.emailExiste(usuario.getEmail())) {
        result.rejectValue("email", "error.usuario", "Ya existe un usuario con ese correo");
    }

    // Si hay errores, regresar al formulario de registro
    if (result.hasErrors()) {
        // volvemos a poner los mismos atributos que en el GET
        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", cliente);
        return "Home/registro";
    }

    // Si todo bien, guardamos
    usuario.setRol(Rol.Cliente);
    usuario.setStatus(true);
    Usuario usuarioGuardado = usuarioService.crearOeditar(usuario);

    cliente.setUsuario(usuarioGuardado);
    clienteService.crearOeditar(cliente);

    return "redirect:/login?registroExitoso=true";
}

}
