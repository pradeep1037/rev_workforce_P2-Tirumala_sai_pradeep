package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long employeeId;
    private String name;
    private String email;
    private String role;

    public LoginResponse(String token, Long employeeId, String name, String email, String role) {
        this.token = token;
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
