package esfe.skyfly.Utilidades;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Modelos.Reservas;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class PdfFacturaUtil {

    public byte[] generarPdfFactura(Factura factura) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

            Paragraph title = new Paragraph("Factura", h1);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("ESFE - SkyFly", bold));
            document.add(new Paragraph("Fecha de emisión: " +
                    factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normal));
            document.add(Chunk.NEWLINE);

            Reservas r = factura.getReserva();

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10f);
            tabla.setSpacingAfter(10f);
            tabla.setWidths(new float[]{30, 70});

            addRow(tabla, "N° Factura:", String.valueOf(factura.getIdFactura()), bold, normal);

            // Ajusta el getter real del ID de reserva según tu entidad Reservas
            // Ejemplos: getReservaId(), getId(), getReserva_id()...
            addRow(tabla, "Reserva ID:", trySafeReservaId(r), bold, normal);

            addRow(tabla, "Monto base:", "$ " + factura.getMontoTotal(), bold, normal);
            addRow(tabla, "Impuestos:", "$ " + factura.getImpuestos(), bold, normal);
            addRow(tabla, "Total a pagar:", "$ " + factura.getTotalAPagar(), bold, normal);

            document.add(tabla);

            document.add(new Paragraph("Gracias por su compra. ¡Buen viaje!", normal));
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de la factura", e);
        }
    }

    private String trySafeReservaId(Reservas r) {
        try {
            // Cambia "getReservaId" por el nombre real de tu getter en Reservas
            return String.valueOf(r.getReservaId());
        } catch (Exception ignore) {
            return "N/D";
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c2);
    }
}