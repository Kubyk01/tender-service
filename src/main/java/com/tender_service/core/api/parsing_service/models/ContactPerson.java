package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContactPerson {
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Email")
    private String email;
}
