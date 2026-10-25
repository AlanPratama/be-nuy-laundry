package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueDTO {
    private String day;
    private String date;
    private BigDecimal revenue;
    private Double weightKg;
}
