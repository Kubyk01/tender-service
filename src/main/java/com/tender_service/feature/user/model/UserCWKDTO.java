package com.tender_service.feature.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCWKDTO {

    @JsonProperty("name")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @JsonProperty("surname")
    @Size(max = 50, message = "Surname must not exceed 50 characters")
    private String surname;

    @JsonProperty("email")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty("username")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    private String username;

    @JsonProperty("password")
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters")
    @Pattern(
            regexp = ".*[!@#$%^&*(),.?\":{}|<>].*",
            message = "Password must contain at least one special character"
    )
    private String password;
}
