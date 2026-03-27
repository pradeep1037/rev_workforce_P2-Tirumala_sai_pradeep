package com.rev.app.service;

import com.rev.app.dto.EmployeeDTO;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.dto.RegisterRequest;
import com.rev.app.entity.Employee;
import com.rev.app.entity.LeaveBalance;
import com.rev.app.exception.BadRequestException;
import com.rev.app.mapper.EmployeeMapper;
import com.rev.app.repository.EmployeeRepository;
import com.rev.app.repository.LeaveBalanceRepository;
import com.rev.app.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

        private final AuthenticationManager authenticationManager;
        private final JwtTokenProvider jwtTokenProvider;
        private final EmployeeRepository employeeRepository;
        private final PasswordEncoder passwordEncoder;
        private final EmployeeMapper employeeMapper;
        private final LeaveBalanceRepository leaveBalanceRepository;

        public LoginResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtTokenProvider.generateToken(authentication);

                Employee employee = employeeRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new BadRequestException("Employee not found"));

                return new LoginResponse(
                                token,
                                employee.getEmployeeId(),
                                employee.getName(),
                                employee.getEmail(),
                                employee.getRole().name());
        }

        @Transactional
        public EmployeeDTO register(RegisterRequest request) {
                if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new BadRequestException("Email already registered: " + request.getEmail());
                }

                Employee employee = new Employee();
                employee.setName(request.getName());
                employee.setEmail(request.getEmail());
                employee.setPassword(passwordEncoder.encode(request.getPassword()));
                employee.setRole(Employee.Role.EMPLOYEE);
                employee.setStatus(Employee.EmployeeStatus.ACTIVE);

                Employee saved = employeeRepository.save(employee);

                // Initialize default leave balance
                LeaveBalance balance = new LeaveBalance(saved, 12, 6, 15);
                leaveBalanceRepository.save(balance);

                return employeeMapper.toDto(saved);
        }

        public Employee getCurrentEmployee() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                return employeeRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("Current user not found"));
        }

        public String getSecurityQuestion(String email) {
                Employee employee = employeeRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("Employee not found"));
                if (employee.getSecurityQuestion() == null || employee.getSecurityQuestion().isEmpty()) {
                        throw new BadRequestException("No security question set for this account. Please contact administrator.");
                }
                return employee.getSecurityQuestion();
        }

        public void resetPassword(String email, String answer, String newPassword) {
                Employee employee = employeeRepository.findByEmail(email)
                                .orElseThrow(() -> new BadRequestException("Employee not found"));

                if (employee.getSecurityAnswer() == null || !employee.getSecurityAnswer().equalsIgnoreCase(answer.trim())) {
                        throw new BadRequestException("Incorrect security answer");
                }

                employee.setPassword(passwordEncoder.encode(newPassword));
                employeeRepository.save(employee);
        }
}
