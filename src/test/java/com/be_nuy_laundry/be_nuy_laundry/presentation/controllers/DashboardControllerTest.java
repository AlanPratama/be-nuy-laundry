package com.be_nuy_laundry.be_nuy_laundry.presentation.controllers;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.DashboardUseCase;
import com.be_nuy_laundry.be_nuy_laundry.common.response.ApiResponse;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.DashboardMetricsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardUseCase dashboardUseCase;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("whenGetMetrics_thenReturnDashboardMetricsDTO")
    void whenGetMetrics_thenReturnDashboardMetricsDTO() {
        DashboardMetricsDTO mockMetrics = DashboardMetricsDTO.builder()
                .todayRevenueGrowth("+15%")
                .activeOrdersCount(5L)
                .build();

        when(dashboardUseCase.getMetrics()).thenReturn(mockMetrics);

        ResponseEntity<ApiResponse<DashboardMetricsDTO>> response = dashboardController.getMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getActiveOrdersCount()).isEqualTo(5L);
        assertThat(response.getBody().getData().getTodayRevenueGrowth()).isEqualTo("+15%");
        verify(dashboardUseCase).getMetrics();
    }
}
