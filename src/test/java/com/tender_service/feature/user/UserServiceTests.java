package com.tender_service.feature.user;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.user.model.AuthorizationDTO;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.feature.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

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
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsersInfoSuccess() throws ParseException {
        String token = "Bearer faketoken";
        UserCwk user = new UserCwk();
        user.setEmail(email);

        when(jwtService.getEmailFromToken("faketoken")).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        UserCwk result = userService.getUsersInfo(token);
        assertEquals(user, result);
    }

    @Test
    void testGetUsersInfoUserNotFound() throws ParseException {
        when(jwtService.getEmailFromToken(anyString())).thenReturn(email);
        when(userRepo.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userService.getUsersInfo("Bearer token")
        );
    }

    @Test
    void testGetUsersInfoForRoleByAdmin() {
        UserCwk user = new UserCwk();
        user.setEmail(email);
        List<UserCwk> users = List.of(user);

        Page<UserCwk> page = new PageImpl<>(users);
        when(userRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        List<UserCwk> result = userService.getUsersInfoForRoleByAdmin(
                0, 10, "email", "asc", Map.of("username", "john"), "USER");

        assertEquals(users, result);
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

        Map<String, String> result = userService.updateUserInfo(token, dto);

        assertEquals("accessToken", result.get("accessToken"));
        assertEquals("refreshToken", result.get("refreshToken"));
    }

    @Test
    void testUpdateUserInfoForIdByAdmin() throws ParseException {
        String token = "Bearer faketoken";
        when(jwtService.getEmailFromToken("faketoken")).thenReturn(email);

        UserCwk adminUser = new UserCwk();
        adminUser.setId(1L);
        adminUser.setEmail(email);
        adminUser.setRoles(Set.of(Role.ADMIN));

        UserCwk existingUser = new UserCwk();
        existingUser.setId(1L);
        existingUser.setEmail(email);
        existingUser.setRoles(Set.of(Role.ADMIN));
        existingUser.setPassword("pass");

        UserCwk updatedUser = new UserCwk();
        updatedUser.setUsername("updated");
        updatedUser.setRoles(Set.of(Role.USER));

        when(userRepo.save(any(UserCwk.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(adminUser));
        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserCwk result = userService.updateUserInfoForIdByAdmin(token, 1L, updatedUser);

        assertNotNull(result, "Result should not be null");
        assertEquals("updated", result.getUsername());
    }

    @Test
    void testDeleteUserForIdByAdmin() {
        UserCwk user = new UserCwk();
        user.setRoles(new HashSet<>());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.deleteUserForIdByAdmin(1L);
        assertEquals("Successfully deleted", result);
    }

    @Test
    void testDeleteUserForIdByAdminUserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                userService.deleteUserForIdByAdmin(1L)
        );
    }

    @Test
    void testUpdateOtherAdminForbidden() throws ParseException {
        String token = "Bearer faketoken";
        when(jwtService.getEmailFromToken("faketoken")).thenReturn("admin@example.com");

        UserCwk admin = new UserCwk();
        admin.setId(1L);
        admin.setEmail("admin@example.com");
        admin.setRoles(Set.of(Role.ADMIN));

        UserCwk target = new UserCwk();
        target.setId(2L);
        target.setEmail("otheradmin@example.com");
        target.setRoles(Set.of(Role.ADMIN));

        UserCwk updateDTO = new UserCwk();
        updateDTO.setUsername("newUsername");
        updateDTO.setRoles(Set.of(Role.USER));

        when(userRepo.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(userRepo.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(AccessDeniedException.class, () ->
                userService.updateUserInfoForIdByAdmin(token, 2L, updateDTO)
        );
    }

    @Test
    void testUpdateUserByAdmin_UserNotFound() throws ParseException {
        String token = "Bearer faketoken";
        when(jwtService.getEmailFromToken("faketoken")).thenReturn(email);

        UserCwk admin = new UserCwk();
        admin.setId(1L);
        admin.setEmail(email);
        admin.setRoles(Set.of(Role.ADMIN));

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(admin));
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () ->
                userService.updateUserInfoForIdByAdmin(token, 99L, new UserCwk())
        );
    }

    @Test
    void testUpdateOwnUserByAdmin_IdZero() throws ParseException {
        String token = "Bearer faketoken";
        when(jwtService.getEmailFromToken("faketoken")).thenReturn(email);

        UserCwk admin = new UserCwk();
        admin.setId(1L);
        admin.setEmail(email);
        admin.setRoles(Set.of(Role.ADMIN));
        admin.setPassword("password");

        UserCwk updatedUser = new UserCwk();
        updatedUser.setUsername("updatedName");
        updatedUser.setRoles(Set.of(Role.USER));

        when(userRepo.save(any(UserCwk.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(admin));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserCwk result = userService.updateUserInfoForIdByAdmin(token, 0L, updatedUser);

        assertNotNull(result, "Result should not be null");
        assertEquals("updatedName", result.getUsername());
    }

    @Test
    @DisplayName("Successful registration")
    void registrationSuccess() {
        AuthorizationDTO dto = createAuthorizationDTO();

        when(userRepo.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepo.existsByUsername(dto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        String result = userService.registration(dto);
        assertEquals("User created", result);
    }

    @Test
    @DisplayName("Registration fails when email already exists")
    void registrationFailsWhenEmailExists() {
        AuthorizationDTO dto = createAuthorizationDTO();

        when(userRepo.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ResponseStatusException.class, () ->
                userService.registration(dto)
        );
    }

    private AuthorizationDTO createAuthorizationDTO() {
        return new AuthorizationDTO(
                "John",
                "Doe",
                "test@example.com",
                "password@123",
                "testuser",
                UserStatus.Activate,
                Set.of(Role.TENDERER)
        );
    }
}