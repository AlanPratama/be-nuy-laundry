package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMetricsDTO {
    private Long totalCustomers;
    private Long newCustomersThisMonth;
    private Long activeCustomers;
}
