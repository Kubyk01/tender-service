package com.tender_service.feature.user.controller;

import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.feature.user.model.AuthorizationDTO;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Create a new user", description = "Adds a new user to the database")
    @PostMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> register(
            @Valid @RequestBody AuthorizationDTO request
    ) {
        return ResponseEntity.ok(userService.registration(request));
    }

    @GetMapping("")
    public ResponseEntity<UserCwk> getUsersInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth
    ) throws ParseException {
        return ResponseEntity.ok(userService.getUsersInfo(auth));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<UserCwk>> getUsersInfoByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ) {
        return ResponseEntity.ok(userService.getUsersInfoForRoleByAdmin(
                pageNumber, pageSize, sortBy, sortDirection, allParams, null));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/{role}")
    public ResponseEntity<List<UserCwk>> getUsersInfoForRoleByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams,
            @PathVariable(required = false) String role
    ) {
        return ResponseEntity.ok(userService.getUsersInfoForRoleByAdmin(
                pageNumber, pageSize, sortBy, sortDirection, allParams, role));
    }

    @PatchMapping()
    public ResponseEntity<Map<String, String>> updateUserInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @Valid @RequestBody UserCWKDTO UserCWKDTO
    ) throws ParseException {
        return ResponseEntity.ok(userService.updateUserInfo(auth, UserCWKDTO));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/admin")
    public ResponseEntity<UserCwk> updateUserInfoForIdByAdmin(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @RequestParam(required = false, defaultValue = "0") Long id,
            @RequestBody UserCwk userCwk
    ) throws ParseException {
        return ResponseEntity.ok(userService.updateUserInfoForIdByAdmin(auth, id, userCwk));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin")
    public ResponseEntity<String> deleteUserForIdByAdmin(
            @RequestParam Long id
    ) {
        return ResponseEntity.ok(userService.deleteUserForIdByAdmin(id));
    }
}