package com.ats.service.impl;

import com.ats.dto.ApplicationStatusSummaryDTO;
import com.ats.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class SummaryServiceImpl implements SummaryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<ApplicationStatusSummaryDTO> getSummaryBetweenDates(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT status, COUNT(*) as total_applications, " +
                    "ROUND((COUNT(*) * 100.0 / (SELECT COUNT(*) FROM applications " +
                    "WHERE application_date BETWEEN ? AND ?)), 2) as percentage " +
                    "FROM applications " +
                    "WHERE application_date BETWEEN ? AND ? " +
                    "GROUP BY status " +
                    "ORDER BY total_applications DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ApplicationStatusSummaryDTO dto = new ApplicationStatusSummaryDTO();
            dto.setStatus(rs.getString("status"));
            dto.setTotalApplications(rs.getLong("total_applications"));
            dto.setPercentage(rs.getDouble("percentage"));
            return dto;
        }, startDate, endDate, startDate, endDate);
    }

    @Override
    public byte[] generateSummaryPDF(LocalDate startDate, LocalDate endDate) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(250, 750);
                contentStream.showText("Application Status Summary Report");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(250, 730);
                contentStream.showText(String.format("Period: %s to %s", startDate, endDate));
                contentStream.endText();

                // Draw table headers
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Status");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Total Applications");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText("Percentage");
                contentStream.endText();

                // Add data rows
                List<ApplicationStatusSummaryDTO> summary = getSummaryBetweenDates(startDate, endDate);
                float y = 680;
                for (ApplicationStatusSummaryDTO dto : summary) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(dto.getStatus());
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(dto.getTotalApplications().toString());
                    contentStream.newLineAtOffset(150, 0);
                    contentStream.showText(String.format("%.2f%%", dto.getPercentage()));
                    contentStream.endText();
                    y -= 20;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
} 