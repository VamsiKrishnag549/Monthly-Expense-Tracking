package com.expensetracker.scheduler;

import java.time.YearMonth;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.service.EmailService;

@Component
public class MonthlyReportScheduler {

    private final UserRepository userRepo;
    private final EmailService emailService;

    public MonthlyReportScheduler(
            UserRepository userRepo,
            EmailService emailService) {
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    // 🔁 Runs on 1st day of every month at 1 AM
    @Scheduled(cron = "0 0 1 1 * ?")
    public void sendMonthlyReports() {

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        List<User> users = userRepo.findAll();

        for (User user : users) {

            // ✅ ONLY USERS WHO CONFIGURED REPORT EMAIL
            if (user.getReportEmail() == null || user.getReportEmail().isBlank()) {
                continue;
            }

            // ✅ loginEmail → fetch expenses
            // ✅ reportEmail → send mail
            emailService.sendMonthlyReport(
                    user.getEmail(),
                    user.getReportEmail(),
                    lastMonth
            );
        }
    }
}
