package com.expensetracker.service;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public ExpenseService(ExpenseRepository expenseRepo, UserRepository userRepo) {
        this.expenseRepo = expenseRepo;
        this.userRepo = userRepo;
    }

    public Expense addExpense(String email, Expense expense) {
        User user = userRepo.findByEmail(email).orElseThrow();
        expense.setUser(user);
        return expenseRepo.save(expense);
    }

    public List<Expense> getExpensesForMonth(String email, YearMonth month) {
        User user = userRepo.findByEmail(email).orElseThrow();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return expenseRepo.findByUserAndDateBetween(user, start, end);
    }

    public Expense updateExpense(Long id, Expense updated) {
        Expense existing = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        existing.setDate(updated.getDate());
        existing.setCategory(updated.getCategory());
        existing.setAmount(updated.getAmount());
        existing.setDescription(updated.getDescription());

        return expenseRepo.save(existing);
    }

    public void deleteExpense(Long id) {
        expenseRepo.deleteById(id);
    }

    // ✅ CSV WRITER
    public byte[] generateExcel(String email, YearMonth month) {
        List<Expense> expenses = getExpensesForMonth(email, month);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");

            // ✅ Header
            Row header = sheet.createRow(0);
            String[] cols = {"Date", "Category", "Amount", "Description", "Type"};

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);

                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // ✅ Data rows
            int rowIdx = 1;
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(e.getDate().toString());
                row.createCell(1).setCellValue(e.getCategory());
                row.createCell(2).setCellValue(e.getAmount());
                row.createCell(3).setCellValue(
                    e.getDescription() == null ? "" : e.getDescription()
                );
                row.createCell(4).setCellValue(
                    e.getDate().getDayOfWeek().getValue() >= 6 ? "Weekend" : "Weekday"
                );
            }

            // ✅ AUTO ADJUST COLUMNS (KEY POINT 🔥)
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}
