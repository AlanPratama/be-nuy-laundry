package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByUsername(@Param("username") String username);

    long countByIsActiveTrue();

    long countByRoleIn(java.util.Collection<com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole> roles);

    long countByRole(com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.UserRole role);

}
