package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentTerms {
    @JsonProperty("Days")
    private Integer days;

    public Integer getDays() {
        return days;
    }
}
