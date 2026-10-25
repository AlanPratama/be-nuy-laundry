package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackageJpaRepository extends JpaRepository<PackageEntity, String>, JpaSpecificationExecutor<PackageEntity> {
    Optional<PackageEntity> findByName(String name);

    @EntityGraph(attributePaths = {"benefits", "benefits.benefit"})
    Page<PackageEntity> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"benefits", "benefits.benefit"})
    Page<PackageEntity> findAll(org.springframework.data.jpa.domain.Specification<PackageEntity> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"benefits", "benefits.benefit"})
    Optional<PackageEntity> findById(String id);

    @Query("SELECT P FROM PackageEntity P WHERE P.id IN (:ids)")
    List<PackageEntity> findByIds(@Param("ids") List<String> ids);

    @EntityGraph(attributePaths = {"benefits", "benefits.benefit"})
    @Query("SELECT P FROM PackageEntity P JOIN PackageBenefitEntity PB ON PB.pkg = P WHERE PB.benefit.id = :benefitId")
    List<PackageEntity> findByBenefitId(@Param("benefitId") String benefitId);

    @Query("SELECT COUNT(p) FROM PackageEntity p WHERE p.type = :type")
    Long countByType(@Param("type") com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType type);
}
