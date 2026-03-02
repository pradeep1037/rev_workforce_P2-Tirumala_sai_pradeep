package com.rev.app.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Pattern(regexp = "^(\\+91[\\-\\s]?)?[6789]\\d{9}$", message = "Please provide a valid Indian phone number")
    private String phone;
    private String address;
    private String emergencyContact;
    private String currentPassword;
    private String newPassword;
}
