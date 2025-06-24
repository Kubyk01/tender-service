package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Guarantee {
    @JsonProperty("AmountTitle")
    private boolean amountTitle;
}
