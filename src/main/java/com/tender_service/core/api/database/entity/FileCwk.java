package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "FILES")
@Getter
@Setter
public class FileCwk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_PATH_NAME")
    private String filePathName;

    @Column(name = "FILE_PATH")
    @JsonIgnore
    private String filePath;

    @Column(name = "FILE_SIZE")
    private int fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENDER_ID", nullable = false)
    @JsonBackReference
    private TenderCwk tender;
}
