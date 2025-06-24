package com.tender_service.core.api.database.repository;

import com.tender_service.core.api.database.entity.UserCwk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserCwk, Long> , JpaSpecificationExecutor<UserCwk> {

    Optional<UserCwk> findByEmail(String email);

    Optional<UserCwk> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    UserCwk findUserCwkById(Long id);
}