package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.CustomerEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, String>, JpaSpecificationExecutor<CustomerEntity> {

    @EntityGraph(attributePaths = {"transactions", "transactions.transactionDetails"})
    Optional<CustomerEntity> findById(String id);

}
