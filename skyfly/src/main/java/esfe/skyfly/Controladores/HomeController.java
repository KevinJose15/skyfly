package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "Home/index"; // Vista principal después del login
    }

    @GetMapping("/login")
    public String login() {
        return "Home/formLogin"; // Vista del formulario de login
    }
}
