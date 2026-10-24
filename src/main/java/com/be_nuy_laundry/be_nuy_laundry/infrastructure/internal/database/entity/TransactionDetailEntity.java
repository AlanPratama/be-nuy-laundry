package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transaction_details")
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailEntity extends BaseEntity {

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private String packageName;

    private String packageDescription;

    private Integer packageDaysEstimation;

    @Enumerated(EnumType.STRING)
    private PackageType packageType;

    @Column(nullable = false)
    private LocalDateTime targetDate;

    private LocalDateTime deliveredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private PackageEntity pkg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;

}
