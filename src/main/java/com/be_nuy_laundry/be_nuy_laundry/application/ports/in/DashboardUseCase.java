package com.be_nuy_laundry.be_nuy_laundry.application.ports.in;

import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.DashboardMetricsDTO;

public interface DashboardUseCase {
    DashboardMetricsDTO getMetrics();
}
