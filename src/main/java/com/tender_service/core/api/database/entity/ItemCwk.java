package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "ITEMS")
@Getter
@Setter
public class ItemCwk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "COUNT")
    private String count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENDER_ID")
    @JsonBackReference
    private TenderCwk tender;
}