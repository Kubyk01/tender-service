package com.tender_service.feature.authorization.controller;

import com.tender_service.feature.authorization.model.RefreshDTO;
import com.tender_service.feature.authorization.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authorization API", description = "Registration&login logic")
public class AuthorizationController {

    @Autowired
    private AuthorizationService authorizationService;

    @Operation(summary = "Login", description = "Login to existed user, return jwt token")
    @GetMapping
    public ResponseEntity<Map<String, String>> login(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam String password
    ) {
        return ResponseEntity.ok(authorizationService.login(username, email, password));
    }

    @Operation(summary = "Refresh access", description = "Refreshes access and refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @Valid @RequestBody RefreshDTO request
    ) throws ParseException {
        return ResponseEntity.ok(authorizationService.refresh(request));
    }
}