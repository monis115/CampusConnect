package com.cc.service.report;

import com.cc.DTO.report.ColumnDefDTO;
import com.cc.DTO.report.ReportResponseDTO;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ExportService {

    public byte[] exportToExcel(ReportResponseDTO reportData, String title) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(title);

            // Title Row
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue(title);

            // Header Row
            int rowIdx = 2;
            Row headerRow = sheet.createRow(rowIdx++);
            List<ColumnDefDTO> columns = reportData.getColumns();
            for (int col = 0; col < columns.size(); col++) {
                headerRow.createCell(col).setCellValue(columns.get(col).getLabel());
            }

            // Data Rows
            for (Map<String, Object> dataRow : reportData.getData()) {
                Row row = sheet.createRow(rowIdx++);
                for (int col = 0; col < columns.size(); col++) {
                    String key = columns.get(col).getKey();
                    Object val = dataRow.get(key);
                    row.createCell(col).setCellValue(val != null ? val.toString() : "");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    public byte[] exportToPdf(ReportResponseDTO reportData, String title) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph(title).setFontSize(18));

            List<ColumnDefDTO> columns = reportData.getColumns();
            Table table = new Table(columns.size());

            // Headers
            for (ColumnDefDTO col : columns) {
                table.addHeaderCell(new Cell().add(new Paragraph(col.getLabel())));
            }

            // Data
            for (Map<String, Object> dataRow : reportData.getData()) {
                for (ColumnDefDTO col : columns) {
                    String key = col.getKey();
                    Object val = dataRow.get(key);
                    table.addCell(new Cell().add(new Paragraph(val != null ? val.toString() : "")));
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
