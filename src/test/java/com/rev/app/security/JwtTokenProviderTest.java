package com.rev.app.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    // Plain text secret (must be >= 32 characters for HS256)
    private final String testSecret = "RevWorkforce_Test_JWT_Secret_Key_AtLeast_32Characters";

    private final long testExpirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {

        jwtTokenProvider = new JwtTokenProvider();

        // Inject properties normally loaded by @Value
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", testExpirationMs);
    }

    @Test
    void testGenerateToken() {

        UserDetails userDetails =
                new User("test@revworkforce.com", "password", Collections.emptyList());

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("test@revworkforce.com",
                jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    void testGenerateTokenFromEmail() {

        String token =
                jwtTokenProvider.generateTokenFromEmail("admin@revworkforce.com");

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("admin@revworkforce.com",
                jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    void testValidateToken_ValidToken() {

        String token =
                jwtTokenProvider.generateTokenFromEmail("test@revworkforce.com");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {

        String invalidToken = "invalid.token.string";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {

        // Set very small expiration
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 1L);

        String token =
                jwtTokenProvider.generateTokenFromEmail("test@revworkforce.com");

        Thread.sleep(10);

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testGetEmailFromToken() {

        String email = "employee@revworkforce.com";

        String token =
                jwtTokenProvider.generateTokenFromEmail(email);

        String extractedEmail =
                jwtTokenProvider.getEmailFromToken(token);

        assertEquals(email, extractedEmail);
    }
}