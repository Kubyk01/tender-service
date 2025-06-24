package com.tender_service.feature.authorization.service;

import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.authorization.model.RefreshDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public Map<String, String> login(String username, String email, String password) {
        if ((email == null || email.isEmpty()) && (username == null || username.isEmpty())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email or username must be provided."
            );
        }

        Optional<UserCwk> userOptional;

        if (email != null && !email.isEmpty()) {
            userOptional = userRepo.findByEmail(email);
        } else {
            userOptional = userRepo.findByUsername(username);
        }

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        UserCwk user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return Map.of("accessToken", token, "refreshToken", refreshToken);
    }

    public Map<String, String> refresh(RefreshDTO refreshDTO) throws ParseException {
        String accessToken = refreshDTO.getAccessToken();
        String refreshToken = refreshDTO.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
        }

        if (!jwtService.isTokenExpiredAndValid(accessToken)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or unexpired access token"
            );
        }

        String emailFromRefresh = jwtService.getEmailFromToken(refreshToken);
        String emailFromAccess = jwtService.getEmailFromToken(accessToken);

        if (!emailFromRefresh.equals(emailFromAccess)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token mismatch - refresh and access tokens don't match"
            );
        }

        UserCwk user = userRepo.findByEmail(emailFromRefresh)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + emailFromRefresh));

        String newToken = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        return Map.of("accessToken", newToken, "refreshToken", newRefreshToken);
    }
}