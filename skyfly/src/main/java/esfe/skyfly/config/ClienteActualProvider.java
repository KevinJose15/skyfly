package esfe.skyfly.config;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import esfe.skyfly.Modelos.Cliente;
import esfe.skyfly.Servicios.Interfaces.IClienteService;

@Component
public class ClienteActualProvider {

    private final IClienteService clienteService;

    public ClienteActualProvider(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    public Cliente getClienteActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No autenticado");
        }

        String email = auth.getName(); // usamos email como username
        Cliente cliente = clienteService.buscarPorUsuarioEmail(email);
        if (cliente == null) {
            throw new AccessDeniedException("El usuario autenticado no posee perfil de Cliente");
        }
        return cliente;
    }
}