package com.expensetracker.controller;

import java.time.YearMonth;
import java.util.List;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.expensetracker.model.Expense;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.service.EmailService;
import com.expensetracker.service.ExpenseService;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserRepository userRepo;
    public ExpenseController(
            ExpenseService expenseService,
            JwtUtil jwtUtil,
            EmailService emailService,
            UserRepository userRepo) {

        this.expenseService = expenseService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.userRepo = userRepo;
    }


    // =========================
    // ADD EXPENSE
    // =========================
    @PostMapping
    public Expense addExpense(
            @RequestBody Expense expense,
            HttpServletRequest request) {

        String email = extractEmail(request);
        return expenseService.addExpense(email, expense);
    }

    // =========================
    // GET EXPENSES BY MONTH
    // =========================
    @GetMapping
    public List<Expense> getExpenses(
            @RequestParam int month,
            @RequestParam int year,
            HttpServletRequest request) {

        String email = extractEmail(request);
        return expenseService.getExpensesForMonth(
                email,
                YearMonth.of(year, month)
        );
    }

    // =========================
    // UPDATE EXPENSE
    // =========================
    @PutMapping("/{id}")
    public Expense updateExpense(
            @PathVariable Long id,
            @RequestBody Expense expense) {

        return expenseService.updateExpense(id, expense);
    }

    // =========================
    // DELETE EXPENSE
    // =========================
    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }

    // =========================
    // DOWNLOAD EXCEL
    // =========================
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam int month,
            @RequestParam int year,
            HttpServletRequest request) {

        String email = extractEmail(request);

        byte[] excel = expenseService.generateExcel(
                email,
                YearMonth.of(year, month)
        );

        return ResponseEntity.ok()
                .header(
                    "Content-Disposition",
                    "attachment; filename=expenses-" + year + "-" + month + ".xlsx"
                )
                .header(
                    "Content-Type",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                .body(excel);
    }

    // =========================
    // SEND EMAIL REPORT (IMMEDIATE)
    // =========================
    @PostMapping("/email")
    public ResponseEntity<String> sendEmailReport(
            @RequestParam Integer month,
            @RequestParam Integer year,
            HttpServletRequest request) {

        String loginEmail = extractEmail(request);

        User user = userRepo.findByEmail(loginEmail).orElseThrow();

        String reportEmail = user.getReportEmail();

        if (reportEmail == null || reportEmail.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Report email not configured");
        }

        YearMonth ym = YearMonth.of(year, month);

        // ✅ FETCH EXPENSES USING LOGIN EMAIL
        // ✅ SEND MAIL TO REPORT EMAIL
        emailService.sendMonthlyReport(loginEmail, reportEmail, ym);

        return ResponseEntity.ok("Email sent successfully");
    }




    // =========================
    // JWT → EMAIL (SINGLE SOURCE)
    // =========================
    private String extractEmail(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing");
        }

        String token = authHeader.substring(7);
        return jwtUtil.validateToken(token).getSubject();
    }
}
