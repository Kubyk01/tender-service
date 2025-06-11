package com.tender_service.feature.authorization;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.authorization.model.AuthorizationDTO;
import com.tender_service.feature.authorization.model.RefreshDTO;
import com.tender_service.feature.authorization.service.AuthorizationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

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

        ResponseEntity<?> response = authorizationService.login(null, "test@example.com", "password");

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("accessToken", body.get("accessToken"));
        assertEquals("refreshToken", body.get("refreshToken"));
    }

    @Test
    @DisplayName("Login fails with invalid password")
    void loginFailsWithInvalidPassword() {
        UserCwk user = mockUser();

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        ResponseEntity<?> response = authorizationService.login(null, "test@example.com", "wrong");

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    @DisplayName("Successful registration")
    void registrationSuccess() {
        AuthorizationDTO dto = createAuthorizationDTO();

        when(userRepo.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepo.existsByUsername(dto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(), any(), any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        ResponseEntity<?> response = authorizationService.registration(dto);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("accessToken", body.get("accessToken"));
        assertEquals("refreshToken", body.get("refreshToken"));
    }

    @Test
    @DisplayName("Registration fails when email already exists")
    void registrationFailsWhenEmailExists() {
        AuthorizationDTO dto = createAuthorizationDTO();

        when(userRepo.existsByEmail(dto.getEmail())).thenReturn(true);

        ResponseEntity<?> response = authorizationService.registration(dto);

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email already in use", response.getBody());
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

        ResponseEntity<?> response = authorizationService.refresh(refreshDTO);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("newAccessToken", body.get("accessToken"));
        assertEquals("newRefreshToken", body.get("refreshToken"));
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

    private AuthorizationDTO createAuthorizationDTO() {
        AuthorizationDTO dto = new AuthorizationDTO(
                "John",
                "Doe",
                "test@example.com",
                "password@123",
                "testuser"
        );
        return dto;
    }
}
