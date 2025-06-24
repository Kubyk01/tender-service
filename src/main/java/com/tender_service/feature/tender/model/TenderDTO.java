package com.tender_service.feature.tender.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TenderDTO {

    private Long id;
    private LocalDateTime createdAt;

    private String prozorroNumber;
    private String title;
    private String unit;
    private String procedureType;
    private String produceType;

    private String organizerName;
    private Integer categoryId;
    private String categoryCode;
    private String categoryTitle;
    private String statusTitle;
    private String participantsOfferStatus;
    private String internalStage;

    private Double budgetAmount;
    private String budgetAmountTitle;
    private Boolean withVat;
    private String vatTitle;
    private String currencyTitle;
    private String currencyHtmlTitle;
    private Integer currencyId;

    private boolean guaranteeBank;
    private Long participantCost;

    private LocalDateTime enquiryPeriodStart;
    private LocalDateTime enquiryPeriodEnd;
    private LocalDateTime tenderingPeriodEnd;
    private LocalDateTime auctionStart;

    private String numberDeal;
    private LocalDateTime dateTime;
    private Long amountDeal;

    private Long amountByAccounts;
    private boolean deliveryTermsUponRequest;
    private LocalDate deliveryPeriodTo;
    private Integer paymentTermsDay;
    private String deliveryAddress;

    private Integer cost;
    private String commentary;
}