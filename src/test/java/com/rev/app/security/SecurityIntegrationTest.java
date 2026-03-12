package com.rev.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        // We do NOT need a setUp() method to initialize test users because
        // the DataInitializer bean automatically seeds the database with:
        // "admin@revworkforce.com" (ADMIN)
        // "manager@revworkforce.com" (MANAGER)
        // "employee@revworkforce.com" (EMPLOYEE)

        @Test
        void testPublicRoutes_NoTokenRequired() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void testProtectedRoutes_WithoutToken_Return403() throws Exception {
                mockMvc.perform(get("/api/leaves/my"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testEmployeeAccess() throws Exception {
                mockMvc.perform(get("/api/leaves/my").with(user("employee@revworkforce.com").roles("EMPLOYEE")))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/leaves/team").with(user("employee@revworkforce.com").roles("EMPLOYEE")))
                                .andExpect(status().isForbidden());

                mockMvc.perform(get("/api/admin/employees").with(user("employee@revworkforce.com").roles("EMPLOYEE")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testManagerAccess() throws Exception {
                mockMvc.perform(get("/api/leaves/my").with(user("manager@revworkforce.com").roles("MANAGER")))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/leaves/team").with(user("manager@revworkforce.com").roles("MANAGER")))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/admin/employees").with(user("manager@revworkforce.com").roles("MANAGER")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testAdminAccess() throws Exception {
                mockMvc.perform(get("/api/admin/employees").with(user("admin@revworkforce.com").roles("ADMIN")))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/leaves/team").with(user("admin@revworkforce.com").roles("ADMIN")))
                                .andExpect(status().isOk());
        }
}
