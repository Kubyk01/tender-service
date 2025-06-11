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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(String filePathName) {
        this.filePathName = filePathName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public TenderCwk getTender() {
        return tender;
    }

    public void setTender(TenderCwk tender) {
        this.tender = tender;
    }
}
