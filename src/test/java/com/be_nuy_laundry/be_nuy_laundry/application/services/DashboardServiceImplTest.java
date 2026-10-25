package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.CustomerJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionDetailJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.DashboardMetricsDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionJpaRepository transactionJpaRepository;

    @Mock
    private TransactionDetailJpaRepository transactionDetailJpaRepository;

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("givenYesterdayAndTodayRevenue_whenGetMetrics_thenCalculatePositiveGrowth")
    void givenYesterdayAndTodayRevenue_whenGetMetrics_thenCalculatePositiveGrowth() {
        // Today = 150000, Yesterday = 100000 -> +50%
        when(transactionJpaRepository.sumTotalPriceBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(150000), BigDecimal.valueOf(100000),
                        BigDecimal.valueOf(10000), BigDecimal.valueOf(20000), BigDecimal.valueOf(30000),
                        BigDecimal.valueOf(40000), BigDecimal.valueOf(50000), BigDecimal.valueOf(60000),
                        BigDecimal.valueOf(70000));

        when(transactionDetailJpaRepository.sumQuantityByTypeBetween(eq(PackageType.WEIGHT), any(), any()))
                .thenReturn(15.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0);

        when(transactionJpaRepository.countByStatus(TransactionStatus.IN_PROGRESS)).thenReturn(3L);
        when(transactionJpaRepository.countByStatus(TransactionStatus.DONE)).thenReturn(10L);
        when(customerJpaRepository.countCreatedBetween(any(), any())).thenReturn(4L);

        TransactionEntity trx1 = new TransactionEntity();
        trx1.setId("trx-1");
        trx1.setInvoiceNo("INV-001");
        trx1.setCustomerName("Budi");
        trx1.setStatus(TransactionStatus.IN_PROGRESS);
        trx1.setTargetDate(LocalDateTime.now().plusHours(2));

        TransactionDetailEntity detail1 = new TransactionDetailEntity();
        detail1.setPackageName("Cuci Kiloan");
        detail1.setQuantity(5);
        trx1.setTransactionDetails(List.of(detail1));

        TransactionEntity trx2 = new TransactionEntity();
        trx2.setId("trx-2");
        trx2.setInvoiceNo("INV-002");
        trx2.setCustomerName("Siti");
        trx2.setStatus(TransactionStatus.IN_PROGRESS);
        trx2.setTargetDate(null);
        trx2.setTransactionDetails(Collections.emptyList());

        when(transactionJpaRepository.findTopActiveOrders(eq(TransactionStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(List.of(trx1, trx2));

        DashboardMetricsDTO metrics = dashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTodayRevenue()).isEqualByComparingTo(BigDecimal.valueOf(150000));
        assertThat(metrics.getTodayRevenueGrowth()).isEqualTo("+50%");
        assertThat(metrics.getTodayWeightKg()).isEqualTo(15.5);
        assertThat(metrics.getActiveOrdersCount()).isEqualTo(3L);
        assertThat(metrics.getDoneOrdersCount()).isEqualTo(10L);
        assertThat(metrics.getNewCustomersToday()).isEqualTo(4L);
        assertThat(metrics.getWeeklyRevenue()).hasSize(7);
        assertThat(metrics.getExpressQueue()).hasSize(2);
        assertThat(metrics.getMachines()).hasSize(4);
        assertThat(metrics.getMachines().get(0).getStatus()).isEqualTo("WASHING");
        assertThat(metrics.getMachines().get(1).getStatus()).isEqualTo("WASHING");
        assertThat(metrics.getMachines().get(2).getStatus()).isEqualTo("DRYING");
        assertThat(metrics.getMachines().get(3).getStatus()).isEqualTo("IDLE");
    }

    @Test
    @DisplayName("givenYesterdayHigherThanToday_whenGetMetrics_thenCalculateNegativeGrowth")
    void givenYesterdayHigherThanToday_whenGetMetrics_thenCalculateNegativeGrowth() {
        // Today = 50000, Yesterday = 100000 -> -50%
        when(transactionJpaRepository.sumTotalPriceBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(50000), BigDecimal.valueOf(100000));

        when(transactionJpaRepository.findTopActiveOrders(eq(TransactionStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        DashboardMetricsDTO metrics = dashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTodayRevenueGrowth()).isEqualTo("-50%");
        assertThat(metrics.getMachines().get(0).getStatus()).isEqualTo("IDLE");
        assertThat(metrics.getMachines().get(1).getStatus()).isEqualTo("IDLE");
        assertThat(metrics.getMachines().get(2).getStatus()).isEqualTo("IDLE");
    }

    @Test
    @DisplayName("givenZeroYesterdayAndPositiveToday_whenGetMetrics_thenCalculate100PercentGrowth")
    void givenZeroYesterdayAndPositiveToday_whenGetMetrics_thenCalculate100PercentGrowth() {
        // Today = 50000, Yesterday = 0 -> +100%
        when(transactionJpaRepository.sumTotalPriceBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.valueOf(50000), BigDecimal.ZERO);

        when(transactionJpaRepository.findTopActiveOrders(eq(TransactionStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        DashboardMetricsDTO metrics = dashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTodayRevenueGrowth()).isEqualTo("+100%");
    }

    @Test
    @DisplayName("givenZeroYesterdayAndZeroToday_whenGetMetrics_thenCalculate0PercentGrowth")
    void givenZeroYesterdayAndZeroToday_whenGetMetrics_thenCalculate0PercentGrowth() {
        // Null returns for all
        when(transactionJpaRepository.sumTotalPriceBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);
        when(transactionDetailJpaRepository.sumQuantityByTypeBetween(any(), any(), any())).thenReturn(null);
        when(transactionJpaRepository.countByStatus(any())).thenReturn(null);
        when(customerJpaRepository.countCreatedBetween(any(), any())).thenReturn(null);
        when(transactionJpaRepository.findTopActiveOrders(eq(TransactionStatus.IN_PROGRESS), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        DashboardMetricsDTO metrics = dashboardService.getMetrics();

        assertThat(metrics).isNotNull();
        assertThat(metrics.getTodayRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(metrics.getTodayRevenueGrowth()).isEqualTo("+0%");
        assertThat(metrics.getTodayWeightKg()).isEqualTo(0.0);
        assertThat(metrics.getActiveOrdersCount()).isEqualTo(0L);
        assertThat(metrics.getDoneOrdersCount()).isEqualTo(0L);
        assertThat(metrics.getNewCustomersToday()).isEqualTo(0L);
    }
}
