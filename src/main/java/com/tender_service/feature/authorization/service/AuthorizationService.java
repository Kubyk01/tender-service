package com.tender_service.feature.authorization.service;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.core.api.database.entity.UserStatus;
import com.tender_service.core.api.database.repository.UserRepository;
import com.tender_service.core.configuration.JwtService;
import com.tender_service.feature.authorization.model.AuthorizationDTO;
import com.tender_service.feature.authorization.model.RefreshDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public ResponseEntity<?> login(String username, String email, String password){
        if ((email == null || email.isEmpty()) && (username == null || username.isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email or username must be provided.");
        }

        Optional<UserCwk> userOptional;

        if (email != null && !email.isEmpty()) {
            userOptional = userRepo.findByEmail(email);
        } else {
            userOptional = userRepo.findByUsername(username);
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        UserCwk user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", token,"refreshToken", refreshToken));
    }

    public ResponseEntity<?> registration(AuthorizationDTO request){
        String email = request.getEmail();
        if (userRepo.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
        }

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already in use");
        }

        UserCwk user = new UserCwk();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setUserStatus(UserStatus.NonActivate);
        user.setRoles(Set.of(Role.USER));

        userRepo.save(user);

        String token = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", token,"refreshToken", refreshToken));
    }

    public ResponseEntity<?> refresh(RefreshDTO refreshDTO) throws ParseException {
        String accessToken = refreshDTO.getAccessToken();
        String refreshToken = refreshDTO.getRefreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or unexpired refresh token");
        }

        if (!jwtService.isTokenExpiredAndValid(accessToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or unexpired access token");
        }

        String email = jwtService.getEmailFromToken(refreshToken);
        if (!Objects.equals(jwtService.getEmailFromToken(accessToken), email)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token or access token");
        }

        UserCwk user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String newToken = jwtService.generateToken(user.getEmail(), user.getUsername(), user.getRoles());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(Map.of("accessToken", newToken,"refreshToken", newRefreshToken));
    }
}
