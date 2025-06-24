package com.tender_service.core.configuration;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTests {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenUserBanned_thenForbiddenResponseSent() throws Exception {
        String token = "jwt.token";
        String email = "banned@example.com";

        UserCwk user = new UserCwk();
        user.setEmail(email);
        user.setUserStatus(UserStatus.Banned);
        user.setRoles(Set.of(Role.USER));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token)).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        when(response.isCommitted()).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        writer.flush();

        String expected = String.format("{\"UserStatus\":\"%s\"}", user.getUserStatus());
        assertTrue(stringWriter.toString().contains(expected));

        verify(filterChain, never()).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenUserBanned_thenForbiddenResponse() throws Exception {
        String token = "jwt.token";
        String email = "banned@example.com";

        UserCwk user = new UserCwk();
        user.setEmail(email);
        user.setUserStatus(UserStatus.Banned);
        user.setRoles(Set.of(Role.USER));

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(token)).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);

        when(response.isCommitted()).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json");
        writer.flush();

        String expected = String.format("{\"UserStatus\":\"%s\"}", user.getUserStatus());
        assertTrue(stringWriter.toString().contains(expected));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void whenNoAuthorizationHeader_thenFilterChainProceed() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void whenInvalidToken_thenThrowsException() throws ParseException {
        String token = "invalid.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.getEmailFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        assertThrows(RuntimeException.class, () -> {
            jwtFilter.doFilterInternal(request, response, filterChain);
        });

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @AfterEach
    void teardown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();
    }
}
