package com.tender_service.feature.user.controller;

import com.tender_service.core.api.database.entity.UserCwk;
import com.tender_service.feature.user.model.UserCWKDTO;
import com.tender_service.feature.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<?> getUsersInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth
    ){
        return userService.getUsersInfo(auth);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> getUsersInfoByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams
    ){
        return userService.getUsersInfoForRoleByAdmin(pageNumber, pageSize, sortBy, sortDirection, allParams, null);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/{role}")
    public ResponseEntity<?> getUsersInfoForRoleByAdmin(
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Map<String, String> allParams,
            @PathVariable(required = false) String role
    ){
        return userService.getUsersInfoForRoleByAdmin(pageNumber, pageSize, sortBy, sortDirection, allParams, role);
    }

    @PatchMapping()
    public ResponseEntity<?> updateUserInfo(
            @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
            @Valid @RequestBody UserCWKDTO UserCWKDTO
    ){
        return userService.updateUserInfo(auth, UserCWKDTO);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PatchMapping("/admin")
    public ResponseEntity<?> updateUserInfoForIdByAdmin(
            @RequestParam Long id,
            @RequestBody UserCwk userCwk
    ){
        return userService.updateUserInfoForIdByAdmin(id, userCwk);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("")
    public ResponseEntity<?> deleteUserForIdByAdmin(
            @RequestParam Long id
    ){
        return userService.deleteUserForIdByAdmin(id);
    }
}
