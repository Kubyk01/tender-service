package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ParsedTenderDTO {

    @JsonProperty("ProzorroNumber")
    private String prozorroNumber;

    @JsonProperty("Organizer")
    private Organizer organizer;

    @JsonProperty("ProcedureType")
    private String procedureType;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Category")
    private Category category;

    @JsonProperty("StatusTitle")
    private String statusTitle;

    @JsonProperty("Budget")
    private Budget budget;

    @JsonProperty("ImportantDates")
    private ImportantDates importantDates;

    @JsonProperty("Nomenclatures")
    private List<Nomenclatures> nomenclaturesList;

    @JsonProperty("ParticipationCostAmount")
    private Long participationCost;

    @JsonProperty("PaymentTerms")
    private List<PaymentTerms> paymentTerms;

    @JsonProperty("Guarantee")
    private Guarantee guarantee;

    public String getProzorroNumber() {
        return prozorroNumber;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public String getTitle() {
        return title;
    }

    public Category getCategory() {
        return category;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public Budget getBudget() {
        return budget;
    }

    public ImportantDates getImportantDates() {
        return importantDates;
    }

    public List<Nomenclatures> getNomenclaturesList() {
        return nomenclaturesList;
    }

    public Long getParticipationCost() {
        return participationCost;
    }

    public List<PaymentTerms> getPaymentTerms() {
        return paymentTerms;
    }

    public Guarantee getGuarantee() {
        return guarantee;
    }

    public String getProcedureType() {
        return procedureType;
    }
}

