package esfe.skyfly.Utilidades;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Modelos.Usuario;
import esfe.skyfly.Servicios.Interfaces.IClienteService;
import esfe.skyfly.Servicios.Interfaces.IUsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final IUsuarioService usuarioService;
    private final IClienteService clienteService;

    public SecurityUtils(IUsuarioService usuarioService, IClienteService clienteService) {
        this.usuarioService = usuarioService;
        this.clienteService = clienteService;
    }

    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String email = auth.getName(); // username = email
        return usuarioService.buscarPorEmail(email).orElse(null);
    }

    /** Devuelve el Cliente asociado al usuario logueado actual. */
    public Cliente getClienteActual() {
        Usuario u = getUsuarioActual();
        if (u == null) return null;
        return clienteService.buscarPorUsuarioId(u.getId()).orElse(null);
    }
}
