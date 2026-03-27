package com.rev.app.integration;

import com.rev.app.dto.CreateEmployeeRequest;
import com.rev.app.dto.LoginRequest;
import com.rev.app.dto.LoginResponse;
import com.rev.app.dto.UpdateProfileRequest;
import com.rev.app.entity.Employee;
import com.rev.app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmployeeRepository employeeRepository;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setEmail("admin@revworkforce.com");
        adminLogin.setPassword("Admin@123");
        ResponseEntity<LoginResponse> adminRes = restTemplate.postForEntity("/api/auth/login", adminLogin,
                LoginResponse.class);
        adminToken = "Bearer " + adminRes.getBody().getToken();

        LoginRequest empLogin = new LoginRequest();
        empLogin.setEmail("employee@revworkforce.com");
        empLogin.setPassword("Employee@123");
        ResponseEntity<LoginResponse> empRes = restTemplate.postForEntity("/api/auth/login", empLogin,
                LoginResponse.class);
        employeeToken = "Bearer " + empRes.getBody().getToken();
    }

    private HttpHeaders getHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void testAdminCanCreateEmployee() {
        CreateEmployeeRequest req = new CreateEmployeeRequest();
        req.setName("New Hired Guy");
        req.setEmail("newhire3@gmail.com");
        req.setPassword("password123");
        req.setRole("EMPLOYEE");
        req.setPhone("9876543210");
        req.setAddress("123 Tech Park");
        req.setJoiningDate(LocalDate.now());
        req.setSalary(50000.0);

        HttpEntity<CreateEmployeeRequest> entity = new HttpEntity<>(req, getHeaders(adminToken));
        ResponseEntity<Map> response = restTemplate.exchange("/api/admin/employees", HttpMethod.POST, entity,
                Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Hired Guy", response.getBody().get("name"));
    }

    @Test
    void testAdminCanGetAllEmployees() {
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders(adminToken));
        ResponseEntity<List> response = restTemplate.exchange("/api/admin/employees", HttpMethod.GET, entity,
                List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // At minimum the 3 seeded employees are returned
        assertTrue(response.getBody().size() >= 3);
    }

    @Test
    void testAdminCanSearchEmployees() {
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders(adminToken));
        // The search endpoint should return 200 OK with a valid (possibly empty) list
        ResponseEntity<List> response = restTemplate.exchange("/api/admin/employees?search=xyz123notfound",
                HttpMethod.GET, entity, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody()); // List is not null (may be empty — search contract)
    }

    @Test
    void testAdminCanDeactivateAndReactivateEmployee() {
        Employee target = employeeRepository.findByEmail("employee@revworkforce.com").orElseThrow();

        HttpEntity<Void> entity = new HttpEntity<>(getHeaders(adminToken));

        ResponseEntity<String> deactivateRes = restTemplate.exchange(
                "/api/admin/employees/" + target.getEmployeeId() + "/deactivate", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.OK, deactivateRes.getStatusCode());

        ResponseEntity<String> reactivateRes = restTemplate.exchange(
                "/api/admin/employees/" + target.getEmployeeId() + "/reactivate", HttpMethod.PUT, entity, String.class);
        assertEquals(HttpStatus.OK, reactivateRes.getStatusCode());
    }

    @Test
    void testEmployeeCanViewOwnProfile() {
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders(employeeToken));
        ResponseEntity<Map> response = restTemplate.exchange("/api/employees/me", HttpMethod.GET, entity, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("employee@revworkforce.com", response.getBody().get("email"));
    }

    @Test
    void testEmployeeCanUpdateOwnProfile() {
        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setPhone("9998887776");
        req.setAddress("New Updated Address");

        HttpEntity<UpdateProfileRequest> entity = new HttpEntity<>(req, getHeaders(employeeToken));
        ResponseEntity<String> response = restTemplate.exchange("/api/employees/me", HttpMethod.PUT, entity,
                String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders(employeeToken));
        ResponseEntity<Map> getResponse = restTemplate.exchange("/api/employees/me", HttpMethod.GET, getEntity,
                Map.class);
        assertEquals("9998887776", getResponse.getBody().get("phone"));
    }

    @Test
    void testEmployeeCanSearchDirectory() {
        HttpEntity<Void> entity = new HttpEntity<>(getHeaders(employeeToken));
        // The directory search returns 200 OK with a valid list (empty for no-match
        // queries)
        ResponseEntity<List> response = restTemplate.exchange("/api/employees/directory?search=xyz123notfound",
                HttpMethod.GET, entity, List.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
