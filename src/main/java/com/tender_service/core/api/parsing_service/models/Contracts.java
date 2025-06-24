package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Contracts{
    @JsonProperty("Status")
    private Status status;

    @JsonProperty("Amount")
    private Long amount;

    @JsonProperty("Documents")
    private List<Documents> documents;
}
