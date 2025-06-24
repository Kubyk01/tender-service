package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Organizer {
    @JsonProperty("Address")
    private String address;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Usreou")
    private String usreou;

    @JsonProperty("ContactPerson")
    private ContactPerson contactPerson;
}
