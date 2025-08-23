package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Cuando alguien entre a "/", lo mandamos a /bienvenida
    @GetMapping("/")
    public String redirectToBienvenida() {
        return "redirect:/bienvenida";
    }

    @GetMapping("/bienvenida")
    public String bienvenida() {
        return "Home/bienvenida"; // tu vista de bienvenida
    }

    @GetMapping("/home")
    public String home() {
        return "Home/index"; // vista principal después del login
    }

    @GetMapping("/login")
    public String login() {
        return "Home/formLogin";
    }
}
