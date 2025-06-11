package com.tender_service.core.api.parsing_service.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
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

    public String getEnquiryPeriodStart() {
        return enquiryPeriodStart;
    }

    public String getEnquiryPeriodEnd() {
        return enquiryPeriodEnd;
    }

    public String getAuctionStart() {
        return auctionStart;
    }

    public String getTenderingPeriodEnd() {
        return tenderingPeriodEnd;
    }

    public void setEnquiryPeriodStart(String enquiryPeriodStart) {
        this.enquiryPeriodStart = enquiryPeriodStart;
    }

    public void setEnquiryPeriodEnd(String enquiryPeriodEnd) {
        this.enquiryPeriodEnd = enquiryPeriodEnd;
    }

    public void setTenderingPeriodEnd(String tenderingPeriodEnd) {
        this.tenderingPeriodEnd = tenderingPeriodEnd;
    }

    public void setAuctionStart(String auctionStart) {
        this.auctionStart = auctionStart;
    }
}
