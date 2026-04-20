package com.employee.employee_system.FilePdf;

import com.employee.employee_system.entity.Employee;
import org.openpdf.text.*;
import org.openpdf.text.Document;
import org.openpdf.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public void generateReport(
            HttpServletResponse resp,
            List<Employee> employees) throws IOException {

        String ts = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition",
                "attachment; filename=\"employee_report_" + ts + ".pdf\"");

        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter writer = PdfWriter.getInstance(
                doc, resp.getOutputStream());

        // Page footer: Page N
        writer.setPageEvent(new PdfPageEventHelper() {
            public void onEndPage(PdfWriter w, Document d) {
                ColumnText.showTextAligned(
                        w.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase("Page " + w.getPageNumber(),
                                FontFactory.getFont(FontFactory.HELVETICA, 8)),
                        (d.right() - d.left()) / 2 + d.leftMargin(),
                        d.bottom() - 10, 0);
            }
        });

        doc.open();

        // Title block
        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        doc.add(new Paragraph("Acme Corp — Employee Report", title));
        doc.add(new Paragraph("Generated: " + LocalDateTime.now()));
        doc.add(new Paragraph("Total records: " + employees.size()));
        doc.add(Chunk.NEWLINE);

        // Table: 7 columns
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 1});

        // Header row
        String[] cols = {"ID", "Full Name", "Email",
                "Dept", "Salary", "Join Date", "Status"};
        Font hf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String col : cols) {
            PdfPCell c = new PdfPCell(new Phrase(col, hf));
            c.setBackgroundColor(new Color(31, 73, 125));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(4);
            table.addCell(c);
        }

        // Data rows
        boolean alt = false;
        for (Employee e : employees) {
            Color bg = alt ? new Color(220, 230, 241) : Color.WHITE;
            Font rf = e.getActive()
                    ? FontFactory.getFont(FontFactory.HELVETICA, 8)
                    : FontFactory.getFont(FontFactory.HELVETICA,
                    8, Font.STRIKETHRU, Color.GRAY);

            addCell(table, String.valueOf(e.getId()),
                    bg, rf, Element.ALIGN_CENTER);
            addCell(table, e.getFirstName() + " " + e.getLastName(),
                    bg, rf, Element.ALIGN_LEFT);
            addCell(table, e.getEmail(),
                    bg, rf, Element.ALIGN_LEFT);
            addCell(table, e.getDepartment(),
                    bg, rf, Element.ALIGN_LEFT);
            addCell(table, "KES " + e.getSalary().toPlainString(),
                    bg, rf, Element.ALIGN_RIGHT);
            addCell(table, e.getDateOfJoining().toString(),
                    bg, rf, Element.ALIGN_CENTER);
            addCell(table, e.getActive() ? "Active" : "Inactive",
                    bg, rf, Element.ALIGN_CENTER);
            alt = !alt;
        }

        doc.add(table);
        doc.close();
    }

    private void addCell(PdfPTable t, String text,
                         Color bg, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setHorizontalAlignment(align);
        c.setPadding(4);
        t.addCell(c);
    }
}