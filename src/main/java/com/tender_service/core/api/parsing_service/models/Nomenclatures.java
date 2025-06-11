package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Nomenclatures {
    @JsonProperty("DeliveryPeriodTo")
    private String deliveryPeriodTo;

    @JsonProperty("DeliveryAddress")
    private String deliveryAdress;

    public String getDeliveryPeriodTo() {
        return deliveryPeriodTo;
    }

    public String getDeliveryAdress() {
        return deliveryAdress;
    }
}
