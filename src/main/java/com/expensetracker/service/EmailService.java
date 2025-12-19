package com.expensetracker.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.YearMonth;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.expensetracker.model.Expense;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ExpenseService expenseService;

    public EmailService(JavaMailSender mailSender,
                        ExpenseService expenseService) {
        this.mailSender = mailSender;
        this.expenseService = expenseService;
    }

    // ============================
    // SEND MONTHLY EXCEL REPORT
    // ============================
    public void sendMonthlyReport(
            String loginEmail,
            String reportEmail,
            YearMonth month) {

        try {
            List<Expense> expenses =
                expenseService.getExpensesForMonth(loginEmail, month);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Expenses");

            Row header = sheet.createRow(0);
            String[] cols = {"Date", "Category", "Amount", "Description", "Type"};

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
            }

            int rowIdx = 1;
            for (Expense e : expenses) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(e.getDate().toString());
                r.createCell(1).setCellValue(e.getCategory());
                r.createCell(2).setCellValue(e.getAmount());
                r.createCell(3).setCellValue(
                    e.getDescription() == null ? "" : e.getDescription()
                );
                r.createCell(4).setCellValue(
                    e.getDate().getDayOfWeek().getValue() >= 6
                        ? "Weekend"
                        : "Weekday"
                );
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setTo(reportEmail); // ✅ ONLY REPORT EMAIL
            helper.setSubject("Monthly Expense Report - " + month);
            helper.setText("Please find attached your report.");

            helper.addAttachment(
                "expenses-" + month + ".xlsx",
                () -> new ByteArrayInputStream(out.toByteArray())
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email failed", e);
        }
    }

}
