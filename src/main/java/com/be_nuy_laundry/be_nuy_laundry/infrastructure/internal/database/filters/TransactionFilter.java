package com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.filters;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionFilter {
    private String name;
    private TransactionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String userId;
    private String customerId;
    private String packageId;
    private com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PaymentStatus paymentStatus;
}
