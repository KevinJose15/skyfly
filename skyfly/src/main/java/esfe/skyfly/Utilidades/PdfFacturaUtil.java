package esfe.skyfly.Utilidades;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;

import esfe.skyfly.Modelos.Factura;
import esfe.skyfly.Modelos.Reservas;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class PdfFacturaUtil {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generarPdfFactura(Factura factura) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 48, 54);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            // Pie de página (Página X)
            writer.setPageEvent(new FooterEvent());

            document.open();

            // ====== Fuentes ======
            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font bold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font small = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
            Font whiteB = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            // ====== HEADER (Logo + Marca + Meta) ======
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 60, 40 });

            // Columna izquierda: logo + marca
            PdfPCell left = noBorderCell();
            // Logo opcional (classpath:/static/logo.png, si existe)
            Image logo = tryLoadLogo("/static/logo.png");
            if (logo != null) {
                logo.scaleToFit(90, 90);
                left.addElement(logo);
                left.addElement(new Phrase("ESFE · SkyFly", bold));
            } else {
                Paragraph brand = new Paragraph("ESFE · SkyFly", h1);
                left.addElement(brand);
            }
            left.addElement(new Phrase("Comprobante de pago", normal));
            header.addCell(left);

            // Columna derecha: meta de factura (card vertical, sin saltos feos)
            PdfPCell right = noBorderCell();

            PdfPTable meta = new PdfPTable(new float[] { 55, 45 }); // etiqueta | valor
            meta.setWidthPercentage(100);

            // Fila 1: N° Factura
            PdfPCell fLbl = new PdfPCell(new Phrase("N° Factura:", bold));
            fLbl.setBorder(Rectangle.BOX);
            fLbl.setBorderColor(new Color(226, 232, 240));
            fLbl.setBackgroundColor(new Color(248, 250, 252));
            fLbl.setPadding(6);
            fLbl.setNoWrap(true);

            PdfPCell fVal = new PdfPCell(new Phrase(String.valueOf(factura.getIdFactura()), normal));
            fVal.setBorder(Rectangle.BOX);
            fVal.setBorderColor(new Color(226, 232, 240));
            fVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            fVal.setPadding(6);
            fVal.setNoWrap(true);

            // Fila 2: Fecha emisión
            PdfPCell dLbl = new PdfPCell(new Phrase("Fecha emisión:", bold));
            dLbl.setBorder(Rectangle.BOX);
            dLbl.setBorderColor(new Color(226, 232, 240));
            dLbl.setBackgroundColor(new Color(248, 250, 252));
            dLbl.setPadding(6);
            dLbl.setNoWrap(true);

            PdfPCell dVal = new PdfPCell(new Phrase(safeDate(factura), normal));
            dVal.setBorder(Rectangle.BOX);
            dVal.setBorderColor(new Color(226, 232, 240));
            dVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            dVal.setPadding(6);
            dVal.setNoWrap(true);

            // Armar tabla
            meta.addCell(fLbl);
            meta.addCell(fVal);
            meta.addCell(dLbl);
            meta.addCell(dVal);

            // Borde suave tipo “card”
            styleAsCard(meta);

            // Insertar en el header
            header.addCell(wrap(meta));

            document.add(header);
            document.add(space(8));

            // Línea divisoria suave
            document.add(ruleLine());

            document.add(space(10));

            Reservas r = factura.getReserva();
            String clienteNombre = leerNombreUsuario(r);
            String paqueteNombre = (r != null && r.getPaquete() != null) ? nz(r.getPaquete().getNombre()) : "N/D";
            String destinoNombre = (r != null && r.getPaquete() != null && r.getPaquete().getDestino() != null)
                    ? nz(r.getPaquete().getDestino().getNombre())
                    : "N/D";

            // ====== BLOQUE: Información de la operación ======
            Paragraph opTitle = new Paragraph("Información de la operación", h2);
            document.add(opTitle);
            document.add(space(6));

            PdfPTable info = new PdfPTable(2);
            info.setWidthPercentage(100);
            info.setWidths(new float[] { 28, 72 });
            addRow(info, "Reserva ID:", trySafeReservaId(r), bold, normal);
            addRow(info, "Cliente:", clienteNombre, bold, normal);
            addRow(info, "Paquete:", paqueteNombre, bold, normal);
            addRow(info, "Destino:", destinoNombre, bold, normal);

            styleAsCard(info);
            document.add(info);

            document.add(space(12));

            // ====== BLOQUE: Resumen de montos ======
            Paragraph sumTitle = new Paragraph("Resumen de montos", h2);
            document.add(sumTitle);
            document.add(space(6));

            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(100);
            // Más aire visual al concepto; importes compactos a la derecha
            totals.setWidths(new float[] { 70, 30 });

            // Encabezados
            totals.addCell(headerCell("Concepto", whiteB));
            PdfPCell importeHeader = headerCell("Importe", whiteB);
            importeHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.addCell(importeHeader);

            // Monto base
            totals.addCell(bodyCell("Monto base"));
            totals.addCell(bodyCellRight("$ " + money(factura.getMontoTotal())));

            // Impuestos
            totals.addCell(bodyCell("Impuestos"));
            totals.addCell(bodyCellRight("$ " + money(factura.getImpuestos())));

            // Total a pagar (destacado)
            PdfPCell totalLbl = bodyCell("Total a pagar");
            totalLbl.setBackgroundColor(new Color(236, 254, 255)); // celeste claro
            totalLbl.setBorderColor(new Color(186, 230, 253));
            totals.addCell(totalLbl);

            PdfPCell totalVal = bodyCellRight("$ " + money(factura.getTotalAPagar()));
            totalVal.setBackgroundColor(new Color(236, 254, 255));
            totalVal.setBorderColor(new Color(186, 230, 253));
            totalVal.setPhrase(new Phrase(
                    "$ " + money(factura.getTotalAPagar()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK)));
            totals.addCell(totalVal);

            // Borde suave tipo “card”
            styleAsCard(totals);
            document.add(totals);

            document.add(space(14));

            // Mensaje de cortesía
            Paragraph thanks = new Paragraph("Gracias por su compra. ¡Buen viaje!", normal);
            thanks.setAlignment(Element.ALIGN_RIGHT);
            document.add(thanks);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de la factura", e);
        }
    }

    // ===== Helpers de estilo y datos =====

    private static class FooterEvent extends PdfPageEventHelper {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase p = new Phrase("Página " + writer.getPageNumber(), footerFont);
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    p,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 10,
                    0);
        }
    }

    private Image tryLoadLogo(String classpathLocation) {
        try (InputStream is = new ClassPathResource(classpathLocation).getInputStream()) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                return Image.getInstance(bytes);
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBackgroundColor(new Color(37, 99, 235)); // azul
        c.setPadding(8);
        c.setBorderColor(new Color(191, 219, 254));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        return c;
    }

    private PdfPCell bodyCell(String text) {
        PdfPCell c = new PdfPCell(
                new Phrase(nz(text), FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY)));
        c.setPadding(8);
        c.setBorderColor(new Color(226, 232, 240));
        return c;
    }

    private PdfPCell bodyCellRight(String text) {
        PdfPCell c = bodyCell(text);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private PdfPCell noBorderCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private PdfPCell wrap(Element element) {
        PdfPCell c = noBorderCell();
        c.addElement(element);
        return c;
    }

    private Paragraph space(float px) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(px);
        p.setSpacingAfter(px);
        return p;
    }

    private LineSeparator ruleLine() {
        LineSeparator sep = new LineSeparator();
        sep.setLineColor(new Color(226, 232, 240));
        sep.setPercentage(100);
        sep.setLineWidth(1f);
        return sep;
    }

    private void styleAsCard(PdfPTable table) {
        // borde y sombra suave emulada con border color
        table.getDefaultCell().setBorderColor(new Color(226, 232, 240));
    }

    private PdfPTable kv(String k, String v, Font kFont, Font vFont) {
        PdfPTable kv = new PdfPTable(2);
        try {
            kv.setWidths(new float[] { 46, 54 });
        } catch (DocumentException ignored) {
        }
        kv.setWidthPercentage(100);

        PdfPCell c1 = new PdfPCell(new Phrase(nz(k), kFont));
        c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(nz(v), vFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        kv.addCell(c1);
        kv.addCell(c2);
        return kv;
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        c1.setBorder(Rectangle.BOX);
        c1.setBorderColor(new Color(226, 232, 240));
        c1.setBackgroundColor(new Color(248, 250, 252));
        c1.setPadding(6);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(nz(value), valueFont));
        c2.setBorder(Rectangle.BOX);
        c2.setBorderColor(new Color(226, 232, 240));
        c2.setPadding(6);
        table.addCell(c2);
    }

    private String trySafeReservaId(Reservas r) {
        try {
            return String.valueOf(Objects.requireNonNull(r).getReservaId());
        } catch (Exception ignore) {
            return "N/D";
        }
    }

    // Lee Usuario.name; si está vacío, cae a email; si no hay nada, N/D
    private String leerNombreUsuario(Reservas r) {
        try {
            if (r != null && r.getCliente() != null && r.getCliente().getUsuario() != null) {
                var u = r.getCliente().getUsuario();
                if (u.getName() != null && !u.getName().isBlank())
                    return u.getName();
                if (u.getEmail() != null && !u.getEmail().isBlank())
                    return u.getEmail();
            }
        } catch (Exception ignore) {
        }
        return "N/D";
    }

    private String nz(String s) {
        return (s == null || s.isBlank()) ? "N/D" : s;
    }

    private String money(BigDecimal v) {
        if (v == null)
            return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safeDate(Factura f) {
        try {
            return f.getFechaEmision().format(DF);
        } catch (Exception e) {
            return "N/D";
        }
    }
}