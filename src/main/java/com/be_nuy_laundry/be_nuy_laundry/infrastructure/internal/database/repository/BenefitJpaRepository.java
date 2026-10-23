package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.BenefitEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BenefitJpaRepository extends JpaRepository<BenefitEntity, String>, JpaSpecificationExecutor<BenefitEntity> {

    Optional<BenefitEntity> findByName(String name);
    
}
