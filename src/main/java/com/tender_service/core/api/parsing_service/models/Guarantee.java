package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Guarantee {
    @JsonProperty("AmountTitle")
    private boolean amountTitle;

    public boolean getAmountTitle() {
        return amountTitle;
    }
}
