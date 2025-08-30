package esfe.skyfly.Controladores;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPortalesController {

    // Dashboard simple para ADMIN
    @GetMapping("/admin/main")
    public String adminMain() {
        return "admin/main";
    }

    // Dashboard simple para AGENTE
    @GetMapping("/agente/main")
    public String agenteMain() {
        return "agente/main";
    }
}