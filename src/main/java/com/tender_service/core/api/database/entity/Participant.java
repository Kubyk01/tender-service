package com.tender_service.core.api.database.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Participant")
@Getter
@Setter
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private CompanyType type;

    @Column(name = "NAME")
    private String name;

}

enum CompanyType {
    ТОВ,
    ФОП
}
