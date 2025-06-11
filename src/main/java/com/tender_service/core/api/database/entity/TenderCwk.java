package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @JoinColumn(name = "USER_ID", nullable = false)
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
    @JoinColumn(name = "SUPPLIER_ID")
    private UserCwk supplier;

    @Column(name = "ORGANIZER_NAME", length = 510)
    private String organizerName;

    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "CATEGORY_TITLE")
    private String categoryTitle;

    @Column(name = "STATUS_TITLE")
    private String statusTitle;

    @Column(name = "PARTICIPANTS_OFFER_STATUS")
    private String participantsOfferStatus;

    @Column(name = "INTERNAL_STAGE")
    private String internalStage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARTICIPANT_ID")
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
    @JoinColumn(name = "TENDERER_ID")
    private UserCwk tenderer;

    @Column(name = "GUARANTEE_BANK")
    private boolean guaranteeBank;

    @Column(name = "PARTICIPANTION_COST")
    private Long participantionCost;

    @Column(name = "ENQUIRY_PERIOD_START")
    private LocalDateTime enquiryPeriodStart;

    @Column(name = "ENQUIRY_PERIOD_END")
    private LocalDateTime enquiryPeriodEnd;

    @Column(name = "TENDERING_PERIOD_END")
    private LocalDateTime tenderingPeriodEnd;

    @Column(name = "AUCTION_START")
    private LocalDateTime auctionStart;

    @Column(name = "AMOUNT_BY_ACCOUNTS")
    private Long amountByAccounts;

    @Column(name = "DELIVERY_TERMS_UPON_REQUEST_BOOLEAN")
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

    @Column(name = "COMMENTARY")
    private String commentary;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private TenderStatus tenderStatus = TenderStatus.CREATED;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserCwk getUser() {
        return user;
    }

    public void setUser(UserCwk user) {
        this.user = user;
    }

    public String getProzorroNumber() {
        return prozorroNumber;
    }

    public void setProzorroNumber(String prozorroNumber) {
        this.prozorroNumber = prozorroNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProduceType() {
        return produceType;
    }

    public void setProduceType(String produceType) {
        this.produceType = produceType;
    }

    public UserCwk getSupplier() {
        return supplier;
    }

    public void setSupplier(UserCwk supplier) {
        this.supplier = supplier;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    public String getParticipantsOfferStatus() {
        return participantsOfferStatus;
    }

    public void setParticipantsOfferStatus(String participantsOfferStatus) {
        this.participantsOfferStatus = participantsOfferStatus;
    }

    public String getInternalStage() {
        return internalStage;
    }

    public void setInternalStage(String internalStage) {
        this.internalStage = internalStage;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public Double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(Double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getBudgetAmountTitle() {
        return budgetAmountTitle;
    }

    public void setBudgetAmountTitle(String budgetAmountTitle) {
        this.budgetAmountTitle = budgetAmountTitle;
    }

    public Boolean getWithVat() {
        return withVat;
    }

    public void setWithVat(Boolean withVat) {
        this.withVat = withVat;
    }

    public String getVatTitle() {
        return vatTitle;
    }

    public void setVatTitle(String vatTitle) {
        this.vatTitle = vatTitle;
    }

    public String getCurrencyTitle() {
        return currencyTitle;
    }

    public void setCurrencyTitle(String currencyTitle) {
        this.currencyTitle = currencyTitle;
    }

    public String getCurrencyHtmlTitle() {
        return currencyHtmlTitle;
    }

    public void setCurrencyHtmlTitle(String currencyHtmlTitle) {
        this.currencyHtmlTitle = currencyHtmlTitle;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public UserCwk getTenderer() {
        return tenderer;
    }

    public void setTenderer(UserCwk tenderer) {
        this.tenderer = tenderer;
    }

    public boolean isGuaranteeBank() {
        return guaranteeBank;
    }

    public void setGuaranteeBank(boolean guaranteeBank) {
        this.guaranteeBank = guaranteeBank;
    }

    public Long getParticipantionCost() {
        return participantionCost;
    }

    public void setParticipantionCost(Long participantionCost) {
        this.participantionCost = participantionCost;
    }

    public LocalDateTime getEnquiryPeriodStart() {
        return enquiryPeriodStart;
    }

    public void setEnquiryPeriodStart(LocalDateTime enquiryPeriodStart) {
        this.enquiryPeriodStart = enquiryPeriodStart;
    }

    public LocalDateTime getEnquiryPeriodEnd() {
        return enquiryPeriodEnd;
    }

    public void setEnquiryPeriodEnd(LocalDateTime enquiryPeriodEnd) {
        this.enquiryPeriodEnd = enquiryPeriodEnd;
    }

    public LocalDateTime getTenderingPeriodEnd() {
        return tenderingPeriodEnd;
    }

    public void setTenderingPeriodEnd(LocalDateTime tenderingPeriodEnd) {
        this.tenderingPeriodEnd = tenderingPeriodEnd;
    }

    public LocalDateTime getAuctionStart() {
        return auctionStart;
    }

    public void setAuctionStart(LocalDateTime auctionStart) {
        this.auctionStart = auctionStart;
    }

    public Long getAmountByAccounts() {
        return amountByAccounts;
    }

    public void setAmountByAccounts(Long amountByAccounts) {
        this.amountByAccounts = amountByAccounts;
    }

    public boolean isDeliveryTermsUponRequest() {
        return deliveryTermsUponRequest;
    }

    public void setDeliveryTermsUponRequest(boolean deliveryTermsUponRequest) {
        this.deliveryTermsUponRequest = deliveryTermsUponRequest;
    }

    public LocalDate getDeliveryPeriodTo() {
        return deliveryPeriodTo;
    }

    public void setDeliveryPeriodTo(LocalDate deliveryPeriodTo) {
        this.deliveryPeriodTo = deliveryPeriodTo;
    }

    public Integer getPaymentTermsDay() {
        return paymentTermsDay;
    }

    public void setPaymentTermsDay(Integer paymentTermsDay) {
        this.paymentTermsDay = paymentTermsDay;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Set<FileCwk> getFiles() {
        return files;
    }

    public void setFiles(Set<FileCwk> files) {
        this.files = files;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public TenderStatus getTenderStatus() {
        return tenderStatus;
    }

    public void setTenderStatus(TenderStatus tenderStatus) {
        this.tenderStatus = tenderStatus;
    }

    public String getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(String procedureType) {
        this.procedureType = procedureType;
    }
}
