package com.tender_service.feature.user;

import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.feature.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTests {

    @InjectMocks
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder passwordEncoder;

    private final String email = "user@example.com";

    @BeforeEach
    void setUp() {
        AutoCloseable closeable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsersInfoSuccess() throws ParseException {
        String token = "Bearer faketoken";
        UserCwk user = new UserCwk();
        user.setEmail(email);
        when(jwtService.getEmailFromToken("faketoken")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userService.getUsersInfo(token);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUsersInfoUserNotFound() throws ParseException {
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.getUsersInfo("Bearer token");
        assertEquals(409, response.getStatusCodeValue());
    }

    @Test
    void testGetUsersInfoForRoleByAdmin() {
        UserCwk user = new UserCwk();
        user.setEmail(email);
        List<UserCwk> users = List.of(user);

        Page<UserCwk> page = new PageImpl<>(users);
        when(userRepo.findAll(ArgumentMatchers.<Specification<UserCwk>>any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<?> response = userService.getUsersInfoForRoleByAdmin(
                0, 10, "email", "asc", Map.of("username", "john"), "USER"
        );
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(users, response.getBody());
    }

    @Test
    void testUpdateUserInfo() throws ParseException {
        String token = "Bearer token";
        UserCwk user = new UserCwk();
        user.setEmail(email);
        user.setPassword("rawPassword");

        UserCWKDTO dto = new UserCWKDTO();
        dto.setUsername("newUser");

        when(jwtService.getEmailFromToken("token")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(anyString(), anyString(), any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refreshToken");

        ResponseEntity<?> response = userService.updateUserInfo(token, dto);
        assertEquals(200, response.getStatusCodeValue());

        Map<String, String> tokens = (Map<String, String>) response.getBody();
        assertEquals("accessToken", tokens.get("accessToken"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    void testUpdateUserInfoForIdByAdmin() {
        UserCwk existingUser = new UserCwk();
        existingUser.setEmail(email);
        existingUser.setPassword("pass");

        UserCwk updatedUser = new UserCwk();
        updatedUser.setUsername("updated");

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<?> response = userService.updateUserInfoForIdByAdmin(1L, updatedUser);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteUserForIdByAdmin() {
        UserCwk user = new UserCwk();
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userService.deleteUserForIdByAdmin(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Succefully deleted", response.getBody());
    }

    @Test
    void testDeleteUserForIdByAdminUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.deleteUserForIdByAdmin(1L);
        assertEquals(409, response.getStatusCodeValue());
    }
}