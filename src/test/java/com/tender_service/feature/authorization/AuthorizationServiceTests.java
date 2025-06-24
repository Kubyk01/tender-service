package com.tender_service.feature.authorization;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.authorization.model.RefreshDTO;
import com.tender_service.feature.authorization.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthorizationServiceTests {

    @InjectMocks
    private AuthorizationService authorizationService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Login success using email")
    void loginSuccessWithEmail() {
        UserCwk user = mockUser();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        Map<String, String> tokens = authorizationService.login(null, "test@example.com", "password");

        assertNotNull(tokens);
        assertEquals("accessToken", tokens.get("accessToken"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    @DisplayName("Login fails with invalid password")
    void loginFailsWithInvalidPassword() {
        UserCwk user = mockUser();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                authorizationService.login(null, "test@example.com", "wrong")
        );
    }

    @Test
    @DisplayName("Login fails when user not found")
    void loginFailsWhenUserNotFound() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                authorizationService.login(null, "test@example.com", "password")
        );
    }

    @Test
    @DisplayName("Login fails with missing credentials")
    void loginFailsWithMissingCredentials() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authorizationService.login(null, null, "password")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Email or username must be provided.", exception.getReason());
    }

    @Test
    @DisplayName("Token refresh success")
    void refreshTokenSuccess() throws ParseException {
        RefreshDTO refreshDTO = new RefreshDTO("expiredToken", "validRefreshToken");
        UserCwk user = mockUser();

        when(jwtService.isRefreshTokenValid(refreshDTO.getRefreshToken())).thenReturn(true);
        when(jwtService.isTokenExpiredAndValid(refreshDTO.getAccessToken())).thenReturn(true);
        when(jwtService.getEmailFromToken(refreshDTO.getRefreshToken())).thenReturn(user.getEmail());
        when(jwtService.getEmailFromToken(refreshDTO.getAccessToken())).thenReturn(user.getEmail());
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("newRefreshToken");

        Map<String, String> tokens = authorizationService.refresh(refreshDTO);

        assertNotNull(tokens);
        assertEquals("newAccessToken", tokens.get("accessToken"));
        assertEquals("newRefreshToken", tokens.get("refreshToken"));
    }

    @Test
    @DisplayName("Token refresh fails with invalid refresh token")
    void refreshTokenFailsWithInvalidRefreshToken() {
        RefreshDTO refreshDTO = new RefreshDTO("expiredToken", "invalidRefreshToken");

        when(jwtService.isRefreshTokenValid(refreshDTO.getRefreshToken())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authorizationService.refresh(refreshDTO)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid or expired refresh token", exception.getReason());
    }

    @Test
    @DisplayName("Token refresh fails with valid access token")
    void refreshTokenFailsWithValidAccessToken() {
        RefreshDTO refreshDTO = new RefreshDTO("validToken", "validRefreshToken");

        when(jwtService.isRefreshTokenValid(refreshDTO.getRefreshToken())).thenReturn(true);
        when(jwtService.isTokenExpiredAndValid(refreshDTO.getAccessToken())).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authorizationService.refresh(refreshDTO)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid or unexpired access token", exception.getReason());
    }

    @Test
    @DisplayName("Token refresh fails with token mismatch")
    void refreshTokenFailsWithTokenMismatch() throws ParseException {
        RefreshDTO refreshDTO = new RefreshDTO("expiredToken", "validRefreshToken");

        when(jwtService.isRefreshTokenValid(refreshDTO.getRefreshToken())).thenReturn(true);
        when(jwtService.isTokenExpiredAndValid(refreshDTO.getAccessToken())).thenReturn(true);
        when(jwtService.getEmailFromToken(refreshDTO.getRefreshToken())).thenReturn("user1@example.com");
        when(jwtService.getEmailFromToken(refreshDTO.getAccessToken())).thenReturn("user2@example.com");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authorizationService.refresh(refreshDTO)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Token mismatch - refresh and access tokens don't match", exception.getReason());
    }

    // Helpers
    private UserCwk mockUser() {
        UserCwk user = new UserCwk();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of(Role.USER));
        user.setUserStatus(UserStatus.Activate);
        return user;
    }
}