package com.rev.app.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String phone;
    private String address;
    private String emergencyContact;
    private String currentPassword;
    private String newPassword;
}
