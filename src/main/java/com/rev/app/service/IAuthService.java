package com.rev.app.service;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.dto.RegisterRequest;
import com.rev.app.entity.Employee;

public interface IAuthService {

    LoginResponse login(LoginRequest request);

    EmployeeDTO register(RegisterRequest request);

    Employee getCurrentEmployee();
    String getSecurityQuestion(String email);
    void resetPassword(String email, String answer, String newPassword);
}
