package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionDetailJpaRepository extends JpaRepository<TransactionDetailEntity, String> {

    @Query(value = "SELECT * FROM transaction_details td WHERE td.transaction_id = :transactionId", nativeQuery = true)
    List<TransactionDetailEntity> findByTransactionId(@Param("transactionId") String transactionId);

    @Query("SELECT COALESCE(SUM(td.quantity), 0) FROM TransactionDetailEntity td WHERE td.packageType = :packageType AND td.createdAt >= :startDate AND td.createdAt < :endDate")
    Double sumQuantityByTypeBetween(
            @Param("packageType") com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType packageType,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}
