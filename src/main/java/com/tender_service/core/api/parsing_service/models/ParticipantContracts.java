package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ParticipantContracts {
    @JsonProperty("ParticipantTitle")
    private String participantTitle;

    @JsonProperty("Contracts")
    private List<Contracts> contracts;
}

