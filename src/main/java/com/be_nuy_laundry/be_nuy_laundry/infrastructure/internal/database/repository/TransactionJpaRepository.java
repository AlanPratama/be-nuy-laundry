package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, String>, JpaSpecificationExecutor<TransactionEntity> {

    @Query(value = "select t.invoice_no from transactions t where t.id = :id", nativeQuery = true)
    String getInvoiceNo(@Param("id") String id);

    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM TransactionEntity t WHERE t.createdAt >= :startDate AND t.createdAt < :endDate")
    java.math.BigDecimal sumTotalPriceBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.status = :status")
    Long countByStatus(@Param("status") com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus status);

    @Query("SELECT t FROM TransactionEntity t WHERE t.status = :status ORDER BY t.targetDate ASC")
    java.util.List<TransactionEntity> findTopActiveOrders(
            @Param("status") com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus status,
            org.springframework.data.domain.Pageable pageable
    );
}
