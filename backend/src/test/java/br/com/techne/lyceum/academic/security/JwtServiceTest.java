package br.com.techne.lyceum.academic.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.techne.lyceum.academic.domain.UserRole;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret =
                Base64.getEncoder()
                        .encodeToString("thisisasecretkeyforjwtsigningatleast256bits".getBytes());
        jwtService = new JwtService(secret, 3600000L);
    }

    @Test
    void generateAndParseToken_roundTripsClaims() {
        UUID publicId = UUID.randomUUID();
        String token = jwtService.generateToken(publicId, "admin@admin", UserRole.ADMIN);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(publicId, jwtService.extractPublicId(token));
        assertEquals("admin@admin", jwtService.extractEmail(token));
        assertEquals(UserRole.ADMIN, jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_whenMalformed_returnsFalse() {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));
    }
}
