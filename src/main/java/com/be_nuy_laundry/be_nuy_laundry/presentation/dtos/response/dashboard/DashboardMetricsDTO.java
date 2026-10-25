package com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    private BigDecimal todayRevenue;
    private String todayRevenueGrowth;
    private Double todayWeightKg;
    private Long activeOrdersCount;
    private Long doneOrdersCount;
    private Long newCustomersToday;
    private List<DailyRevenueDTO> weeklyRevenue;
    private List<ExpressQueueItemDTO> expressQueue;
    private List<MachineStatusDTO> machines;
}
