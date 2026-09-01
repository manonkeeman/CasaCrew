package com.casacrew.service;

import com.casacrew.model.Expense;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ExpensePdfService {

    private static final Locale NL = Locale.forLanguageTag("nl-NL");
    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(NL);
    private static final DateTimeFormatter DATE_NL = DateTimeFormatter.ofPattern("d MMMM yyyy", NL);
    private static final Color BRAND_COLOR = new Color(47, 93, 76);

    public byte[] generate(List<Expense> expenses, String organizationName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader(doc, organizationName);
            doc.add(Chunk.NEWLINE);
            addTable(doc, expenses);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generatie mislukt", e);
        }
    }

    private void addHeader(Document doc, String organizationName) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BRAND_COLOR);
        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);

        doc.add(new Paragraph(organizationName != null ? organizationName : "CasaCrew", titleFont));
        doc.add(new Paragraph("Uitgavenoverzicht", FontFactory.getFont(FontFactory.HELVETICA, 14, Color.GRAY)));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Gegenereerd op: " + java.time.LocalDate.now().format(DATE_NL), subFont));
    }

    private void addTable(Document doc, List<Expense> expenses) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.3f, 1.5f, 3f, 1.2f});
        table.setWidthPercentage(100);

        addHeaderCell(table, "Datum");
        addHeaderCell(table, "Categorie");
        addHeaderCell(table, "Omschrijving");
        addHeaderCell(table, "Bedrag");

        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            addBodyCell(table, e.getExpenseDate() != null ? e.getExpenseDate().format(DATE_NL) : "-");
            addBodyCell(table, e.getCategory());
            addBodyCell(table, e.getDescription());
            addBodyCell(table, EUR.format(e.getAmount()));
            total = total.add(e.getAmount());
        }

        PdfPCell totalLabel = new PdfPCell(new Phrase("Totaal", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        totalLabel.setColspan(3);
        totalLabel.setBorder(Rectangle.TOP);
        totalLabel.setPadding(8);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell totalValue = new PdfPCell(new Phrase(EUR.format(total),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_COLOR)));
        totalValue.setBorder(Rectangle.TOP);
        totalValue.setPadding(8);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(totalLabel);
        table.addCell(totalValue);

        doc.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        cell.setBackgroundColor(BRAND_COLOR);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(6);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }
}
