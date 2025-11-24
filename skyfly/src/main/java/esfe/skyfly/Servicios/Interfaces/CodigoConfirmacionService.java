package esfe.skyfly.Servicios.Interfaces;

import esfe.skyfly.Modelos.CodigoConfirmacion;

public interface CodigoConfirmacionService {
    CodigoConfirmacion crearCodigo(String email);
    boolean validarCodigo(String email, String codigo);
    CodigoConfirmacion crearCodigoYEnviarEmail(String emailCliente,
                                          String nombreCliente,
                                          String nombrePaquete,
                                          java.time.LocalDateTime fechaReserva,
                                          java.math.BigDecimal montoTotal,
                                          String destino);
}