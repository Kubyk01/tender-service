package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "TENDERS")
@Getter
@Setter
public class TenderCwk {

    @Id
    @Column(nullable = false, name = "ID")
    private Long id;

    @CreationTimestamp
    @Column(updatable = false, name = "CREATE_AT")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    @JsonIgnore()
    private UserCwk user;

    @Column(name = "PROZORRO_NUMBER")
    private String prozorroNumber;

    @Column(name = "TITLE", length = 510)
    private String title;

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "PROCEDURE_TYPE")
    private String procedureType;

    @Column(name = "PRODUCE_UNIT_TYPE")
    private String produceType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SUPPLIER_ID") // штивно
    private UserCwk supplier;

    @Column(name = "ORGANIZER_NAME", length = 510)
    private String organizerName;

    @Column(name="ORGANIZER_USREOU")
    private String organizerUsreou;

    @Column(name="ORGANIZER_ADDRESS", length = 510)
    private String organizerAddress;

    @Column(name="CONTACT_PERSON_NAME")
    private String contactPersonName;

    @Column(name="CONTACT_PERSON_PHONE")
    private String contactPersonPhone;

    @Column(name="CONTACT_PERSON_EMAIL")
    private String contactPersonEmail;

    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "CATEGORY_TITLE")
    private String categoryTitle;

    @Column(name = "STATUS_TITLE")
    private String statusTitle;

    @Column(name = "PARTICIPANTS_OFFER_STATUS") //in progress
    private String participantsOfferStatus;

    @Column(name = "INTERNAL_STAGE")  //штивно
    private String internalStage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARTICIPANT_ID") //штивно
    private Participant participant;

    @Column(name = "BUDGET_AMOUNT")
    private Double budgetAmount;

    @Column(name = "BUDGET_AMOUNT_TITLE")
    private String budgetAmountTitle;

    @Column(name = "WITH_VAT")
    private Boolean withVat;

    @Column(name = "VAT_TITLE")
    private String vatTitle;

    @Column(name = "CURRENCY_TITLE")
    private String currencyTitle;

    @Column(name = "CURRENCY_HTML_TITLE")
    private String currencyHtmlTitle;

    @Column(name = "CURRENCY_ID")
    private Integer currencyId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TENDERER_ID") //штивно
    private UserCwk tenderer;

    @Column(name = "GUARANTEE_BANK")
    private boolean guaranteeBank;

    @Column(name = "PARTICIPANT_COST") //штивно
    private Long participantCost;

    @Column(name = "ENQUIRY_PERIOD_START")
    private LocalDateTime enquiryPeriodStart;

    @Column(name = "ENQUIRY_PERIOD_END")
    private LocalDateTime enquiryPeriodEnd;

    @Column(name = "TENDERING_PERIOD_END")
    private LocalDateTime tenderingPeriodEnd;

    @Column(name = "AUCTION_START")
    private LocalDateTime auctionStart;

    @Column(name = "QUALIFICATION_DATE")
    private LocalDateTime qualificationDate;

    @Column(name = "ID_DEAL")
    private String idDeal;

    @Column(name = "DATE_DEAL")
    private LocalDateTime dateDeal;

    @Column(name = "AMOUNT_DEAL")
    private Long amountDeal;

    @Column(name = "URL_DEAL", length = 500)
    private String urlDeal;

    @Column(name = "AMOUNT_BY_ACCOUNTS") //штивно
    private Long amountByAccounts;

    @Column(name = "DELIVERY_TERMS_UPON_REQUEST_BOOLEAN")  //штивно
    private boolean deliveryTermsUponRequest;

    @Column(name = "DELIVERY_PERIOD_TO")
    private LocalDate deliveryPeriodTo;

    @Column(name = "PAYMENT_TERMS_DAY")
    private Integer paymentTermsDay;

    @Column(name = "DELIVERY_ADDRESS", length = 510)
    private String deliveryAddress;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<FileCwk> files;

    @Column(name = "COST")
    private Integer cost;

    @Column(name = "COMMENTARY", length = 1000)
    private String commentary;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private TenderStatus tenderStatus = TenderStatus.CREATED;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ItemCwk> items;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ItemsAndParticipants> itemsAndParticipants;
}
