package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Destino;
import esfe.skyfly.Servicios.Interfaces.IDestinoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DestinoController {

    private final IDestinoService destinoService;

    public DestinoController(IDestinoService destinoService) {
        this.destinoService = destinoService;
    }

    // CLIENTE: solo ver/seleccionar
    @GetMapping("/destinos/index")
    public String destinosCliente(Model model) {
        List<Destino> destinos = destinoService.buscarTodos();
        model.addAttribute("destinos", destinos);
        return "Cliente/destinos/index";
    }

    // ADMIN/AGENTE: CRUD
    @GetMapping("/destinos/mant")
    public String destinosMant(Model model) {
        List<Destino> destinos = destinoService.obtenerTodo();
        model.addAttribute("destinos", destinos);
        return "Destinos/mant";
    }

    // (Opcional) create/edit/delete ya existentes en tu proyecto…
}
