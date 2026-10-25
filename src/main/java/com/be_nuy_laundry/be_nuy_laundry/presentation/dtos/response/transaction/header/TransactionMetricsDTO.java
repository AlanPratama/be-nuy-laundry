package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.transaction.header;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMetricsDTO {
    private Long totalTransactions;
    private Long inProgressCount;
    private Long doneCount;
    private Long canceledCount;
    private BigDecimal totalRevenue;
}
