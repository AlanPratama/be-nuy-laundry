package com.be_nuy_laundry.be_nuy_laundry.domain.model;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetailModel extends BaseModel {
    private String transactionId;
    private String packageId;
    private String packageName;
    private String packageDescription;
    private Integer packageDaysEstimation;
    private Integer quantity;
    private BigDecimal totalPrice;
    private PackageType packageType;
    private LocalDateTime targetDate;
    private LocalDateTime deliveredDate;
    private PackageModel pkg;
    private TransactionModel transaction;
}
