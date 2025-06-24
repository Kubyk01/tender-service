package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Documents {
    @JsonProperty("DateModified")
    private String dateModified;

    @JsonProperty("ViewUrl")
    private String viewUrl;

    @JsonProperty("Id")
    private Long Id;
}
