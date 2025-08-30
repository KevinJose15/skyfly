// ClientePagosController.java
package esfe.skyfly.Controladores;

import esfe.skyfly.Modelos.Pago;
import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Servicios.Interfaces.IPagoService;
import esfe.skyfly.Servicios.Interfaces.IFacturaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;

@Controller
public class ClientePagosController {

    private final IPagoService pagoService;
    private final IFacturaService facturaService;

    public ClientePagosController(IPagoService pagoService, IFacturaService facturaService) {
        this.pagoService = pagoService;
        this.facturaService = facturaService;
    }

    @GetMapping("/cliente/pagos")
    public String misPagos(Authentication auth, Model model) {
        String email = auth.getName();

        // Pagos del cliente
        List<Pago> pagosCliente = pagoService.obtenerTodos().stream()
            .filter(p -> p.getReserva() != null
                      && p.getReserva().getCliente() != null
                      && p.getReserva().getCliente().getUsuario() != null
                      && email.equalsIgnoreCase(p.getReserva().getCliente().getUsuario().getEmail()))
            .sorted(Comparator
                    .comparing(Pago::getFechaPago, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed())
            .collect(Collectors.toList());

        // Conjunto de reservas presentes en los pagos del cliente
        Set<Integer> reservaIds = pagosCliente.stream()
            .map(p -> p.getReserva() != null ? p.getReserva().getReservaId() : null)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Mapa: reservaId -> idFactura (resolvemos 1 a 1 para no depender de p.factura)
        Map<Integer, Integer> facturaPorReservaId = new HashMap<>();
        for (Integer rid : reservaIds) {
            // Si existe 1 factura por reserva:
            facturaService.buscarPorReservaId(rid)
                .map(Factura::getIdFactura)
                .ifPresent(fid -> facturaPorReservaId.put(rid, fid));

            // Si manejas múltiples facturas por reserva, usa esta alternativa:
            // facturaService.buscarUltimaPorReservaId(rid)
            //     .map(Factura::getIdFactura)
            //     .ifPresent(fid -> facturaPorReservaId.put(rid, fid));
        }

        BigDecimal total = pagosCliente.stream()
            .map(Pago::getMonto)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pagos", pagosCliente);
        model.addAttribute("total", total);
        model.addAttribute("facturaPorReservaId", facturaPorReservaId);
        return "cliente/mis_pagos";
    }
}