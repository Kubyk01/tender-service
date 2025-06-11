package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Budget {

    @JsonProperty("Amount")
    private double amount;

    @JsonProperty("AmountTitle")
    private String amountTitle;

    @JsonProperty("WithVat")
    private boolean withVat;

    @JsonProperty("VatTitle")
    private String vatTitle;

    @JsonProperty("CurrencyTitle")
    private String currencyTitle;

    @JsonProperty("CurrencyHtmlTitle")
    private String currencyHtmlTitle;

    @JsonProperty("CurrencyId")
    private int currencyId;

    public double getAmount() {
        return amount;
    }

    public String getAmountTitle() {
        return amountTitle;
    }

    public boolean isWithVat() {
        return withVat;
    }

    public String getVatTitle() {
        return vatTitle;
    }

    public String getCurrencyTitle() {
        return currencyTitle;
    }

    public String getCurrencyHtmlTitle() {
        return currencyHtmlTitle;
    }

    public int getCurrencyId() {
        return currencyId;
    }
}
