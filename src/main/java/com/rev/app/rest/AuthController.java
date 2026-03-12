package com.rev.app.rest;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.dto.RegisterRequest;
import com.rev.app.service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // Store JWT in an HttpOnly cookie so Thymeleaf pages auto-send it
        Cookie jwtCookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
        jwtCookie.setHttpOnly(true); // inaccessible to JavaScript (XSS protection)
        jwtCookie.setPath("/"); // valid for all paths
        jwtCookie.setMaxAge((int) (jwtExpirationMs / 1000)); // same lifetime as JWT
        // jwtCookie.setSecure(true); // uncomment in production (HTTPS only)
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Clear the cookie by setting Max-Age=0
        Cookie jwtCookie = new Cookie("JWT_TOKEN", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/register")
    public ResponseEntity<EmployeeDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/security-question")
    public ResponseEntity<String> getSecurityQuestion(@RequestParam String email) {
        String question = authService.getSecurityQuestion(email);
        return ResponseEntity.ok(question);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody com.rev.app.dto.ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getAnswer(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully. Please login with your new password.");
    }
}
