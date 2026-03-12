package com.rev.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String answer;
    @NotBlank
    private String newPassword;
}
