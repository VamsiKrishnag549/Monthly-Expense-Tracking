package com.expensetracker.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.expensetracker.model.User;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {

        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        userService.register(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        User dbUser = userService.login(
                user.getEmail(),
                user.getPassword()
        );

        // ✅ FIXED TOKEN GENERATION
        String token = jwtUtil.generateToken(
                dbUser.getId(),
                dbUser.getEmail()
        );

        return ResponseEntity.ok(token);
    }
}
