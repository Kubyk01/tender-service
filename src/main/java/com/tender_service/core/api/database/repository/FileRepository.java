package com.tender_service.core.api.database.repository;

import com.tender_service.core.api.database.entity.FileCwk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileCwk, Long> {
    void deleteByTenderIdAndFilePathName(Long tenderId, String savedFileName);

    void deleteByTenderId(Long tenderId);
}
