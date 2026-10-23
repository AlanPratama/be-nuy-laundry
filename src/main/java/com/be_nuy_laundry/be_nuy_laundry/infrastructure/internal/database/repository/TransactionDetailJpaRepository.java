package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionDetailJpaRepository extends JpaRepository<TransactionDetailEntity, String> {

    @Query(value = "SELECT * FROM transaction_details td WHERE td.transaction_id = :transactionId", nativeQuery = true)
    List<TransactionDetailEntity> findByTransactionId(@Param("transactionId") String transactionId);

}
