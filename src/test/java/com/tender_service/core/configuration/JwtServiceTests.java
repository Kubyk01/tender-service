package com.tender_service.core.configuration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.tender_service.core.api.database.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {

    private JwtService jwtService;

    private final String SECRET = "01234567890123456789012345678901";
    private final String REFRESH_SECRET = "abcdefghijklmnopqrstuvwxyz123456";

    @BeforeEach
    void setup() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
        ReflectionTestUtils.setField(jwtService, "REFRESH_SECRET", REFRESH_SECRET);
    }

    @Test
    void testGenerateTokenAndGetEmail() throws ParseException {
        String email = "test@example.com";
        String username = "testuser";
        Set<Role> roles = Set.of(Role.USER);

        String token = jwtService.generateToken(email, username, roles);
        assertNotNull(token);

        String extractedEmail = jwtService.getEmailFromToken(token);
        assertEquals(email, extractedEmail);

        SignedJWT jwt = SignedJWT.parse(token);
        assertEquals(email, jwt.getJWTClaimsSet().getSubject());
        assertEquals(username, jwt.getJWTClaimsSet().getStringClaim("username"));

        List<String> rolesClaim = (List<String>) jwt.getJWTClaimsSet().getClaim("roles");
        Set<Role> rolesFromToken = rolesClaim.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        assertEquals(roles, rolesFromToken);
    }

    @Test
    void testGenerateRefreshTokenAndGetEmail() throws ParseException {
        String email = "refresh@example.com";

        String refreshToken = jwtService.generateRefreshToken(email);
        assertNotNull(refreshToken);

        String extractedEmail = jwtService.getEmailFromToken(refreshToken);
        assertEquals(email, extractedEmail);
    }

    @Test
    void testIsTokenValidWithValidToken() {
        String token = jwtService.generateToken("valid@example.com", "user", Set.of(Role.USER));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testIsTokenValidWithInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtService.isTokenValid(invalidToken));
    }

    @Test
    void testIsRefreshTokenValidWithValidToken() {
        String refreshToken = jwtService.generateRefreshToken("refresh@example.com");
        assertTrue(jwtService.isRefreshTokenValid(refreshToken));
    }

    @Test
    void testIsRefreshTokenValidWithInvalidToken() {
        String invalidRefreshToken = "invalid.refresh.token";
        assertFalse(jwtService.isRefreshTokenValid(invalidRefreshToken));
    }

    @Test
    void testIsTokenExpiredAndValid() throws Exception {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "SECRET", SECRET);
        ReflectionTestUtils.setField(jwtService, "REFRESH_SECRET", REFRESH_SECRET);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("expire@example.com")
                .claim("username", "user")
                .claim("roles", Set.of(Role.USER))
                .issueTime(new Date(System.currentTimeMillis() - 2000)) // issued 2 seconds ago
                .expirationTime(new Date(System.currentTimeMillis() - 1000)) // expired 1 second ago
                .build();

        JWSSigner signer = new MACSigner(SECRET.getBytes());
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        String expiredToken = signedJWT.serialize();

        assertTrue(jwtService.isTokenExpiredAndValid(expiredToken));
        assertFalse(jwtService.isTokenValid(expiredToken));
    }

    @Test
    void testGetEmailFromTokenThrowsParseException() {
        String invalidToken = "not.a.jwt.token";

        assertThrows(ParseException.class, () -> jwtService.getEmailFromToken(invalidToken));
    }
}
