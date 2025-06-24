package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ITEMS_AND_PARTICIPANTS")
@Getter
@Setter
public class ItemsAndParticipants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENDER_ID", nullable = false, updatable = false)
    @JsonBackReference
    private TenderCwk tender;

    @Column(name = "SUPPLIER")
    private String supplier;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountNumber;

    @Column(name = "DATE")
    private LocalDateTime date;

    @Column(name = "ITEM_STATUS")
    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;
}

enum ItemStatus {
    рах_запрошено,
    рах_отримано,
    рах_сплачено,
    заказ_на_поставку_зроблено,
    товар_отримано
}
