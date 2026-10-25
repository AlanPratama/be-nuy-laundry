package com.be_nuy_laundry.be_nuy_laundry.application.services;

import com.be_nuy_laundry.be_nuy_laundry.application.ports.in.DashboardUseCase;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionDetailEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.TransactionEntity;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.PackageType;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.entity.enumrated.TransactionStatus;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.CustomerJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionDetailJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.infrastructure.internal.database.repository.TransactionJpaRepository;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.DailyRevenueDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.DashboardMetricsDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.ExpressQueueItemDTO;
import com.be_nuy_laundry.be_nuy_laundry.presentation.dtos.response.dashboard.MachineStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardUseCase {

    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionDetailJpaRepository transactionDetailJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;

    private static final String[] INDO_DAYS = {"Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab"};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM", new Locale("id", "ID"));
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm 'WIB'");

    @Override
    public DashboardMetricsDTO getMetrics() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();

        // 1. Today Revenue & Growth
        BigDecimal todayRevenue = transactionJpaRepository.sumTotalPriceBetween(startOfToday, endOfToday);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        BigDecimal yesterdayRevenue = transactionJpaRepository.sumTotalPriceBetween(startOfYesterday, startOfToday);
        if (yesterdayRevenue == null) yesterdayRevenue = BigDecimal.ZERO;

        String growthStr = "+0%";
        if (yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = todayRevenue.subtract(yesterdayRevenue);
            BigDecimal pct = diff.multiply(BigDecimal.valueOf(100)).divide(yesterdayRevenue, 0, RoundingMode.HALF_UP);
            growthStr = (pct.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + pct + "%";
        } else if (todayRevenue.compareTo(BigDecimal.ZERO) > 0) {
            growthStr = "+100%";
        }

        // 2. Today Weight
        Double todayWeight = transactionDetailJpaRepository.sumQuantityByTypeBetween(PackageType.WEIGHT, startOfToday, endOfToday);
        if (todayWeight == null) todayWeight = 0.0;

        // 3. Order Counts
        Long activeOrdersCount = transactionJpaRepository.countByStatus(TransactionStatus.IN_PROGRESS);
        if (activeOrdersCount == null) activeOrdersCount = 0L;

        Long doneOrdersCount = transactionJpaRepository.countByStatus(TransactionStatus.DONE);
        if (doneOrdersCount == null) doneOrdersCount = 0L;

        Long newCustomersToday = customerJpaRepository.countCreatedBetween(startOfToday, endOfToday);
        if (newCustomersToday == null) newCustomersToday = 0L;

        // 4. Weekly Revenue (last 7 days)
        List<DailyRevenueDTO> weeklyRevenue = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            LocalDateTime s = d.atStartOfDay();
            LocalDateTime e = d.atTime(LocalTime.MAX);

            BigDecimal rev = transactionJpaRepository.sumTotalPriceBetween(s, e);
            if (rev == null) rev = BigDecimal.ZERO;

            Double wt = transactionDetailJpaRepository.sumQuantityByTypeBetween(PackageType.WEIGHT, s, e);
            if (wt == null) wt = 0.0;

            int dayOfWeek = d.getDayOfWeek().getValue() % 7; // Sunday = 0, Monday = 1
            weeklyRevenue.add(DailyRevenueDTO.builder()
                    .day(INDO_DAYS[dayOfWeek])
                    .date(d.format(DATE_FMT))
                    .revenue(rev)
                    .weightKg(wt)
                    .build());
        }

        // 5. Express Queue (Active in-progress orders ordered by target date)
        List<TransactionEntity> activeOrders = transactionJpaRepository.findTopActiveOrders(
                TransactionStatus.IN_PROGRESS,
                PageRequest.of(0, 5)
        );

        LocalDateTime now = LocalDateTime.now();
        List<ExpressQueueItemDTO> expressQueue = new ArrayList<>();
        for (TransactionEntity trx : activeOrders) {
            String pkgName = "Layanan Laundry";
            Double qty = 1.0;
            if (trx.getTransactionDetails() != null && !trx.getTransactionDetails().isEmpty()) {
                TransactionDetailEntity det = trx.getTransactionDetails().get(0);
                if (det.getPackageName() != null) pkgName = det.getPackageName();
                if (det.getQuantity() != null) qty = det.getQuantity().doubleValue();
            }

            long remainingMinutes = 0L;
            if (trx.getTargetDate() != null) {
                Duration dur = Duration.between(now, trx.getTargetDate());
                remainingMinutes = Math.max(0, dur.toMinutes());
            }

            String deadline = trx.getTargetDate() != null ? trx.getTargetDate().format(TIME_FMT) : "-";

            expressQueue.add(ExpressQueueItemDTO.builder()
                    .id(trx.getId())
                    .invoiceNumber(trx.getInvoiceNo())
                    .customerName(trx.getCustomerName())
                    .packageName(pkgName)
                    .quantity(qty)
                    .deadlineTime(deadline)
                    .remainingMinutes(remainingMinutes)
                    .status(trx.getStatus().name())
                    .build());
        }

        // 6. Machines Status (Computed operational status)
        List<MachineStatusDTO> machines = List.of(
                MachineStatusDTO.builder()
                        .id("m-1")
                        .name("Mesin Washer #1 (Speed Queen)")
                        .capacityKg(10)
                        .status(!activeOrders.isEmpty() ? "WASHING" : "IDLE")
                        .remainingMinutes(!activeOrders.isEmpty() ? 18 : null)
                        .currentInvoice(!activeOrders.isEmpty() ? activeOrders.get(0).getInvoiceNo() : null)
                        .build(),
                MachineStatusDTO.builder()
                        .id("m-2")
                        .name("Mesin Washer #2 (Speed Queen)")
                        .capacityKg(10)
                        .status(activeOrders.size() > 1 ? "WASHING" : "IDLE")
                        .remainingMinutes(activeOrders.size() > 1 ? 32 : null)
                        .currentInvoice(activeOrders.size() > 1 ? activeOrders.get(1).getInvoiceNo() : null)
                        .build(),
                MachineStatusDTO.builder()
                        .id("m-3")
                        .name("Mesin Dryer #1 (Gas Commercial)")
                        .capacityKg(12)
                        .status(!activeOrders.isEmpty() ? "DRYING" : "IDLE")
                        .remainingMinutes(!activeOrders.isEmpty() ? 14 : null)
                        .currentInvoice(!activeOrders.isEmpty() ? activeOrders.get(0).getInvoiceNo() : null)
                        .build(),
                MachineStatusDTO.builder()
                        .id("m-4")
                        .name("Mesin Washer #3 (Jumbo 20kg)")
                        .capacityKg(20)
                        .status("IDLE")
                        .build()
        );

        return DashboardMetricsDTO.builder()
                .todayRevenue(todayRevenue)
                .todayRevenueGrowth(growthStr)
                .todayWeightKg(todayWeight)
                .activeOrdersCount(activeOrdersCount)
                .doneOrdersCount(doneOrdersCount)
                .newCustomersToday(newCustomersToday)
                .weeklyRevenue(weeklyRevenue)
                .expressQueue(expressQueue)
                .machines(machines)
                .build();
    }
}
