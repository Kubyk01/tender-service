package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class ImportantDates {
    @JsonProperty("EnquiryPeriodStart")
    private String enquiryPeriodStart;

    @JsonProperty("EnquiryPeriodEnd")
    private String enquiryPeriodEnd;

    @JsonProperty("TenderingPeriodEnd")
    private String tenderingPeriodEnd;

    @JsonProperty("AuctionStart")
    private String auctionStart;
}
