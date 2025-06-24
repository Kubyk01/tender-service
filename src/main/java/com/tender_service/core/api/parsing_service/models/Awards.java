package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Awards {
    @JsonProperty("Status")
    private String status;

    @JsonProperty("ParticipantTitle")
    private String participantTitle;

    @JsonProperty("ComplaintPeriodStart")
    private LocalDateTime complaintPeriodStart;
}
