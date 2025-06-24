package com.tender_service.feature.authorization.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class RefreshDTO {
    @NotBlank(message = "Cant be empty")
    String accessToken;

    @NotBlank(message = "Cant be empty")
    String refreshToken;
}
