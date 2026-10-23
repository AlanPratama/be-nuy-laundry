package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, String>, JpaSpecificationExecutor<TransactionEntity> {

    @Query(value = "select t.invoice_no from transactions t where t.id = :id", nativeQuery = true)
    String getInvoiceNo(@Param("id") String id);

}
