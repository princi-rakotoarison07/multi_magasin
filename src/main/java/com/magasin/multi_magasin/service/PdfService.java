package com.magasin.multi_magasin.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.magasin.multi_magasin.domain.entity.Vente;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PdfService {

    public byte[] generateVentesPdf(List<Vente> ventes, String type, String dateRange, String username) throws DocumentException {
        Document document;
        if ("detail".equals(type)) {
            document = new Document(PageSize.A4.rotate());
        } else {
            document = new Document(PageSize.A4);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        // Font
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font fontSubHeader = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontTable = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font fontTableBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        // Header
        Paragraph header = new Paragraph("MULTI MAGASIN", fontHeader);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Paragraph subHeader = new Paragraph("Rapport des ventes - " + dateRange, fontSubHeader);
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingAfter(20);
        document.add(subHeader);

        // Table
        PdfPTable table;
        if ("detail".equals(type)) {
            table = new PdfPTable(5); // Ref, Client, Total, Paiement, Date
            table.setWidths(new int[]{2, 4, 3, 3, 4});
        } else {
            table = new PdfPTable(4); // Ref, Client, Total, Date
            table.setWidths(new int[]{3, 5, 4, 4});
        }
        table.setWidthPercentage(100);

        // Table Header
        addTableHeader(table, type, fontTableBold);

        // Table Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        double totalAmount = 0;
        
        for (Vente v : ventes) {
            table.addCell(new Phrase("V-" + v.getId(), fontTable));
            
            String clientName = (v.getClient() != null) ? v.getClient().getNom() + " " + (v.getClient().getPrenom() != null ? v.getClient().getPrenom() : "") : "Client Passage";
            table.addCell(new Phrase(clientName, fontTable));
            
            PdfPCell totalCell = new PdfPCell(new Phrase(v.getTotal() + " Ar", fontTable));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
            
            if ("detail".equals(type)) {
                table.addCell(new Phrase(v.getStatut(), fontTable));
            }
            
            table.addCell(new Phrase(v.getCreatedAt().format(formatter), fontTable));
            
            if (v.getTotal() != null) {
                totalAmount += v.getTotal().doubleValue();
            }
        }
        document.add(table);

        // Summary
        document.add(Chunk.NEWLINE);
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        summaryTable.addCell(new Phrase("Nombre de ventes:", fontTableBold));
        summaryTable.addCell(new Phrase(String.valueOf(ventes.size()), fontTable));
        
        summaryTable.addCell(new Phrase("Montant Total:", fontTableBold));
        PdfPCell totalSumCell = new PdfPCell(new Phrase(String.format("%.2f Ar", totalAmount), fontTableBold));
        totalSumCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.addCell(totalSumCell);

        if ("detail".equals(type) && !ventes.isEmpty()) {
            summaryTable.addCell(new Phrase("Moyenne par vente:", fontTableBold));
             PdfPCell avgCell = new PdfPCell(new Phrase(String.format("%.2f Ar", totalAmount / ventes.size()), fontTable));
             avgCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.addCell(avgCell);
        }

        document.add(summaryTable);

        // Footer
        Paragraph footer = new Paragraph("Généré par: " + username + " le " + LocalDateTime.now().format(formatter), FontFactory.getFont(FontFactory.HELVETICA, 8));
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String type, Font font) {
        Stream.of("Référence", "Client", "Total").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase(columnTitle, font));
            table.addCell(header);
        });
        
        if ("detail".equals(type)) {
             PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase("Paiement", font));
            table.addCell(header);
        }

        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase("Date", font));
        table.addCell(header);
    }
}
