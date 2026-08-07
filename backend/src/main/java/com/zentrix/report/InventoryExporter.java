package com.zentrix.report;

import com.zentrix.device.dto.DeviceResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Genera el reporte de inventario en PDF o XLSX — docs/04, sección 7.
 */
@Component
public class InventoryExporter {

    private static final String[] HEADERS = {"IMEI", "Modelo", "Versión Android", "Estado", "Grupo", "Última conexión"};

    public byte[] toXlsx(List<DeviceResponse> devices) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i]);
            }
            int rowIndex = 1;
            for (DeviceResponse device : devices) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(device.imei());
                row.createCell(1).setCellValue(nullToEmpty(device.model()));
                row.createCell(2).setCellValue(nullToEmpty(device.androidVersion()));
                row.createCell(3).setCellValue(device.status());
                row.createCell(4).setCellValue(nullToEmpty(device.groupName()));
                row.createCell(5).setCellValue(device.lastSeenAt() != null ? device.lastSeenAt().toString() : "");
            }
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] toPdf(List<DeviceResponse> devices) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = page.getMediaBox().getHeight() - 50;
            float leftMargin = 40;
            float rowHeight = 16;

            content.beginText();
            content.setFont(boldFont, 14);
            content.newLineAtOffset(leftMargin, y);
            content.showText("Reporte de Inventario — Zentrix");
            content.endText();
            y -= 30;

            content.beginText();
            content.setFont(boldFont, 9);
            content.newLineAtOffset(leftMargin, y);
            content.showText(String.join("   |   ", HEADERS));
            content.endText();
            y -= rowHeight;

            content.setFont(font, 9);
            for (DeviceResponse device : devices) {
                if (y < 50) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    content = new PDPageContentStream(document, page);
                    y = page.getMediaBox().getHeight() - 50;
                    content.setFont(font, 9);
                }
                String line = String.join("   |   ", device.imei(), nullToEmpty(device.model()),
                        nullToEmpty(device.androidVersion()), device.status(), nullToEmpty(device.groupName()),
                        device.lastSeenAt() != null ? device.lastSeenAt().toString() : "");
                content.beginText();
                content.newLineAtOffset(leftMargin, y);
                content.showText(line);
                content.endText();
                y -= rowHeight;
            }
            content.close();

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
