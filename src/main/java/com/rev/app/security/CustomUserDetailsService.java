package com.rev.app.security;

import com.rev.app.entity.Employee;
import com.rev.app.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found with email: " + email));

        if (employee.getStatus() == Employee.EmployeeStatus.INACTIVE) {
            throw new UsernameNotFoundException("Account is deactivated: " + email);
        }

        return new User(
                employee.getEmail(),
                employee.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name())));
    }
}
