package com.tender_service.feature.user.model;

import com.tender_service.core.api.database.entity.Role;
import com.tender_service.core.api.database.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Data
@Getter
@Setter
public class AuthorizationDTO {
    @NotBlank(message = "Cant be empty")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @NotBlank(message = "Cant be empty")
    @Size(max = 50, message = "Surname must not exceed 50 characters")
    private String surname;

    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Cant be empty")
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters")
    @Pattern(
            regexp = ".*[!@#$%^&*(),.?\":{}|<>].*",
            message = "Password must contain at least one special character"
    )
    private String password;

    @NotBlank(message = "Cant be empty")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    private UserStatus userStatus = UserStatus.NonActivate;

    private Set<Role> roles = Set.of();

    public AuthorizationDTO(String name, String surname, String email, String password, String username, UserStatus userStatus, Set<Role> roles) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.username = username;
        this.userStatus = userStatus;
        this.roles = roles;
    }
}
