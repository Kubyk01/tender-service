package com.tender_service.core.api.database.repository;

import com.tender_service.core.api.database.entity.TenderCwk;
import com.tender_service.core.api.database.entity.UserCwk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderRepository extends JpaRepository<TenderCwk, Long>, JpaSpecificationExecutor<TenderCwk> {

    boolean existsById(Long id);

    @Query("SELECT t FROM TenderCwk t WHERE t.id = :id AND (t.user = :user OR t.supplier = :user OR t.tenderer = :user)")
    TenderCwk findByIdAndUserInAnyRole(@Param("id") Long id, @Param("user") UserCwk user);

    TenderCwk findTenderCwkById(Long id);

    boolean existsByUserAndId(UserCwk user, Long id);

    TenderCwk findTenderCwkByUserAndId(UserCwk user, Long id);

    @Query("SELECT DISTINCT t.unit FROM TenderCwk t WHERE t.unit IS NOT NULL")
    List<String> findAllDistinctUnits();
}