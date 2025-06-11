package com.tender_service.core.configuration;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String email;

            try {
                email = jwtService.getEmailFromToken(jwt);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"unvalid token\"}");
                response.getWriter().flush();
                return;
            }

            userRepository.findByEmail(email).ifPresent(user -> {
                if (!jwtService.isTokenValid(jwt)) {
                    try {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"invalid token\"}");
                        response.getWriter().flush();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to send status response", e);
                    }
                    return;
                }

                if (user.getUserStatus() != UserStatus.Activate && !user.getRoles().contains(Role.ADMIN)) {
                    try {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        String message = String.format("{\"UserStatus\":\"%s\"}", user.getUserStatus());
                        response.getWriter().write(message);
                        response.getWriter().flush();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to send status response", e);
                    }
                    return;
                }

                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .toList();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        if (!response.isCommitted()) {
            filterChain.doFilter(request, response);
        }
    }
}