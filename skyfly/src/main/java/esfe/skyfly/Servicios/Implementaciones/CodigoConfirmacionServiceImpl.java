package esfe.skyfly.Servicios.Implementaciones;

import esfe.skyfly.Servicios.Interfaces.CodigoConfirmacionService;
import esfe.skyfly.Modelos.CodigoConfirmacion;
import esfe.skyfly.Repositorios.CodigoConfirmacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
public class CodigoConfirmacionServiceImpl implements CodigoConfirmacionService {

    private static final Logger logger = LoggerFactory.getLogger(CodigoConfirmacionServiceImpl.class);

    private final CodigoConfirmacionRepository codigoRepo;
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:SkyFly <no-reply@skyfly.com>}")
    private String from;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    public CodigoConfirmacionServiceImpl(CodigoConfirmacionRepository codigoRepo,
                                         JavaMailSender mailSender) {
        this.codigoRepo = codigoRepo;
        this.mailSender = mailSender;
    }

    // --- Método original (mantener compatibilidad) ---
    @Override
    public CodigoConfirmacion crearCodigo(String email) {
        CodigoConfirmacion codigo = new CodigoConfirmacion();
        codigo.setEmail(email);

        String codigoGenerado = String.format("%06d", new Random().nextInt(1_000_000));
        codigo.setCodigo(codigoGenerado);
        codigo.setFechaGeneracion(LocalDateTime.now());
        codigo.setUsado(false);

        CodigoConfirmacion guardado = codigoRepo.save(codigo);

        try {
            enviarCorreoCodigoBasico(email, codigoGenerado);
            logger.info("Correo (básico) enviado correctamente a {}", email);
        } catch (Exception ex) {
            logger.error("Error al enviar correo (básico) a {} — motivo: {}", email, ex.getMessage(), ex);
        }

        return guardado;
    }

    // --- Nuevo: crear código y enviar correo con plantilla completa ---
    public CodigoConfirmacion crearCodigoYEnviarEmail(
            String emailCliente,
            String nombreCliente,
            String nombrePaquete,
            LocalDateTime fechaReserva,
            BigDecimal montoTotal,
            String destino
    ) {
        // crear y guardar código
        CodigoConfirmacion codigo = new CodigoConfirmacion();
        codigo.setEmail(emailCliente);

        String codigoGenerado = String.format("%06d", new Random().nextInt(1_000_000));
        codigo.setCodigo(codigoGenerado);
        codigo.setFechaGeneracion(LocalDateTime.now());
        codigo.setUsado(false);

        CodigoConfirmacion guardado = codigoRepo.save(codigo);

        // enviar correo con plantilla
        try {
            enviarCorreoConPlantilla(emailCliente, nombreCliente, nombrePaquete,
                    fechaReserva, montoTotal, destino, codigoGenerado);
            logger.info("Correo de confirmación enviado a {}", emailCliente);
        } catch (Exception ex) {
            logger.error("Error al enviar correo a {}: {}", emailCliente, ex.getMessage(), ex);
            // opcional: marca en BD que envío falló
        }

        return guardado;
    }

    // Versión básica si se llama al método antiguo
    private void enviarCorreoCodigoBasico(String email, String codigo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(email);
        helper.setFrom(from != null && !from.isBlank() ? from : mailUsername);
        helper.setSubject("Código de confirmación de pago - SkyFly");

        String texto = String.format(
                "Hola,\n\nTu código de confirmación de pago es: %s\n\n" +
                "Ingresa este código en la pantalla de validación para completar tu reserva.\n\n" +
                "Si no realizaste este pago, ignora este mensaje.\n\nSkyFly ✈",
                codigo
        );
        helper.setText(texto, false);

        mailSender.send(message);
    }

    // Plantilla HTML completa con tu mensaje y datos de reserva
    private void enviarCorreoConPlantilla(
            String emailCliente,
            String nombreCliente,
            String nombrePaquete,
            LocalDateTime fechaReserva,
            BigDecimal montoTotal,
            String destino,
            String codigo
    ) throws MessagingException {

        DateTimeFormatter fechaSolo = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fechaConHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaFormateada = fechaReserva != null ? fechaReserva.format(fechaSolo) : "N/A";
        String expiracion = LocalDateTime.now().plusMinutes(15).format(fechaConHora);

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
        String montoFormateado = montoTotal != null ? nf.format(montoTotal) : "$0.00";

        String cuerpoHtml = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Estimado/a <b>%s</b>,</p>

                    <p>Gracias por elegir <b>SkyFly</b>, tu sitio web de confianza para descubrir el mundo 🌍.</p>

                    <p>
                        Hemos recibido tu solicitud de pago para la reserva correspondiente al paquete 
                        <b>%s</b>. Para validar y completar esta transacción de forma segura, 
                        por favor ingresa el siguiente código de confirmación en nuestro portal:
                    </p>

                    <h2 style="text-align:center; background:#f2f2f2; padding:10px; border-radius:8px;">
                        🔐 Código de confirmación:<br>
                        <b style="font-size: 28px; letter-spacing: 4px;">%s</b>
                    </h2>

                    <p>
                        Este código es válido por los próximos <b>15 minutos</b> (hasta %s). Si no has iniciado esta operación, 
                        por favor contáctanos de inmediato para garantizar la seguridad de tu cuenta.
                    </p>

                    <h3>Detalles de la reserva:</h3>
                    <ul>
                        <li><b>Cliente:</b> %s</li>
                        <li><b>Fecha:</b> %s</li>
                        <li><b>Monto Total:</b> %s</li>
                        <li><b>Destino:</b> %s</li>
                    </ul>

                    <p>Si tienes alguna duda o necesitas asistencia, nuestro equipo está disponible para ayudarte.</p>

                    <p>¡Gracias por confiar en nosotros para tu próxima aventura!</p>

                    <p><b>SkyFly</b></p>

                    <p>
                        ✉️ contacto@SkyFly.com<br>
                        📞 +503 2222 0000<br>
                        🌐 www.SkyFly.com
                    </p>
                </div>
                """.formatted(
                nombreCliente != null ? nombreCliente : "Cliente",
                nombrePaquete != null ? nombrePaquete : "Paquete",
                codigo,
                expiracion,
                nombreCliente != null ? nombreCliente : "Cliente",
                fechaFormateada,
                montoFormateado,
                destino != null ? destino : "Destino"
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(emailCliente);
        helper.setFrom(from != null && !from.isBlank() ? from : mailUsername);
        helper.setSubject("Código de confirmación de pago - SkyFly");
        helper.setText(cuerpoHtml, true); // true -> HTML

        mailSender.send(message);
    }

    @Override
    public boolean validarCodigo(String email, String codigo) {
        Optional<CodigoConfirmacion> opt = codigoRepo.findByEmailAndCodigo(email, codigo);
        if (opt.isPresent()) {
            CodigoConfirmacion encontrado = opt.get();
            if (!encontrado.isUsado()) {
                encontrado.setUsado(true);
                codigoRepo.save(encontrado);
                return true;
            }
        }
        return false;
    }
}
