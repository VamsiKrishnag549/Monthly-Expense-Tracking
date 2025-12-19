package com.expensetracker.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.model.User;
@RestController
@RequestMapping("/api/users")
public class UserController {

    // OPTIONAL — for future use
    @GetMapping("/health")
    public String health() {
        return "User controller working";
    }
    
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public UserController(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    // ✅ SAVE REPORT EMAIL
    @PostMapping("/report-email")
    public void saveReportEmail(
            @RequestParam String reportEmail,
            HttpServletRequest request
    ) {
        String auth = request.getHeader("Authorization");
        String token = auth.substring(7);
        String loginEmail = jwtUtil.validateToken(token).getSubject();

        User user = userRepo.findByEmail(loginEmail).orElseThrow();
        user.setReportEmail(reportEmail);

        userRepo.save(user);
    }
    
    
}
